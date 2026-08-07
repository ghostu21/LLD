package com.carrental.lld.reservation;

import com.carrental.lld.addon.ReservationAddon;
import com.carrental.lld.billing.Bill;
import com.carrental.lld.billing.BillItem;
import com.carrental.lld.billing.BillItemType;
import com.carrental.lld.billing.BillingService;
import com.carrental.lld.events.AsyncEventBus;
import com.carrental.lld.events.RentalEvent;
import com.carrental.lld.events.RentalEventType;
import com.carrental.lld.log.VehicleLogService;
import com.carrental.lld.log.VehicleLogType;
import com.carrental.lld.vehicle.Vehicle;
import com.carrental.lld.vehicle.VehicleInventory;
import com.carrental.lld.vehicle.VehicleStatus;
import com.carrental.lld.vehicle.VehicleType;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Core reservation orchestrator with per-vehicle locking and overlap checks.
 * <p>
 * Why: concurrent bookings for the same vehicle must be serialized; overlap
 * detection for CONFIRMED/ACTIVE reservations prevents double-booking.
 * <p>
 * Logic: in-memory repositories; {@link ReentrantLock} per barcode; reserve
 * under lock → overlap check → CONFIRMED + vehicle RESERVED → event publish.
 */
public class ReservationService {
    private static final long LOCK_TIMEOUT_SECONDS = 5L;

    private final VehicleInventory inventory;
    private final BillingService billingService;
    private final VehicleLogService logService;
    private final AsyncEventBus eventBus;
    private final CancellationPolicy cancellationPolicy;

    private final Map<String, VehicleReservation> reservations = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> vehicleLocks = new ConcurrentHashMap<>();

    /**
     * @param inventory           vehicle repository
     * @param billingService      bill generation
     * @param logService          vehicle event logs
     * @param eventBus            async notifications
     * @param cancellationPolicy  fee strategy
     */
    public ReservationService(VehicleInventory inventory, BillingService billingService,
                            VehicleLogService logService, AsyncEventBus eventBus,
                            CancellationPolicy cancellationPolicy) {
        this.inventory = inventory;
        this.billingService = billingService;
        this.logService = logService;
        this.eventBus = eventBus;
        this.cancellationPolicy = cancellationPolicy;
    }

    /**
     * Reserves a vehicle for the given window under per-vehicle lock.
     *
     * @param memberId         renting member
     * @param vehicleBarcode   target vehicle
     * @param start            rental start
     * @param end              rental end
     * @param pickupBranchId   pickup branch
     * @param returnBranchId   return branch
     * @param addons           selected add-ons
     * @param additionalDrivers extra drivers
     * @return confirmed reservation with generated bill
     */
    public VehicleReservation reserve(String memberId, String vehicleBarcode,
                                      LocalDateTime start, LocalDateTime end,
                                      String pickupBranchId, String returnBranchId,
                                      List<ReservationAddon> addons,
                                      List<String> additionalDrivers) {
        ReentrantLock lock = vehicleLocks.computeIfAbsent(vehicleBarcode, k -> new ReentrantLock());
        boolean acquired = false;
        try {
            acquired = lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                throw new ReservationTimeoutException(vehicleBarcode);
            }

            Vehicle vehicle = inventory.findByBarcode(vehicleBarcode);
            if (vehicle.getStatus() == VehicleStatus.MAINTENANCE) {
                throw new VehicleNotAvailableException(vehicleBarcode, "vehicle in maintenance");
            }

            if (hasOverlappingReservation(vehicleBarcode, start, end, null)) {
                throw new VehicleNotAvailableException(vehicleBarcode,
                        "overlapping reservation exists");
            }

            String reservationNumber = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            VehicleReservation reservation = new VehicleReservation(
                    reservationNumber, memberId, vehicleBarcode, start, end,
                    pickupBranchId, returnBranchId, addons, additionalDrivers);

            Bill bill = billingService.generateBill(reservation, vehicle);
            reservations.put(reservationNumber, reservation);
            vehicle.setStatus(VehicleStatus.RESERVED);

            eventBus.publish(new RentalEvent(
                    RentalEventType.RESERVATION_CONFIRMED,
                    reservationNumber, memberId, vehicleBarcode,
                    vehicle.getMake() + " " + vehicle.getModel() + " " + start + " to " + end));

            return reservation;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReservationTimeoutException(vehicleBarcode);
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }

    /**
     * Cancels a reservation and applies cancellation policy fee.
     *
     * @param reservationNumber reservation to cancel
     * @return updated reservation
     */
    public VehicleReservation cancel(String reservationNumber) {
        VehicleReservation reservation = getRequired(reservationNumber);
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return reservation;
        }
        if (reservation.getStatus() == ReservationStatus.COMPLETED
                || reservation.getStatus() == ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Cannot cancel reservation in status: " + reservation.getStatus());
        }

        ReentrantLock lock = vehicleLocks.computeIfAbsent(
                reservation.getVehicleBarcode(), k -> new ReentrantLock());
        lock.lock();
        try {
            double fee = cancellationPolicy.computeCancellationFee(
                    reservation, reservation.getBill(), LocalDateTime.now());
            if (fee > 0 && reservation.getBill() != null) {
                reservation.getBill().addItem(new BillItem(
                        BillItemType.FINE, "Cancellation fee", fee));
            }

            reservation.setStatus(ReservationStatus.CANCELLED);
            Vehicle vehicle = inventory.findByBarcode(reservation.getVehicleBarcode());
            vehicle.setStatus(VehicleStatus.AVAILABLE);

            eventBus.publish(new RentalEvent(
                    RentalEventType.RESERVATION_CANCELLED,
                    reservationNumber, reservation.getMemberId(),
                    reservation.getVehicleBarcode(),
                    "Cancelled, fee: $" + String.format("%.2f", fee)));

            return reservation;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Marks pickup: reservation ACTIVE, vehicle RENTED, logs PICKUP.
     *
     * @param reservationNumber reservation id
     * @return updated reservation
     */
    public VehicleReservation pickup(String reservationNumber) {
        VehicleReservation reservation = getRequired(reservationNumber);
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Pickup only allowed from CONFIRMED, was: "
                    + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setPickupTime(Instant.now());

        Vehicle vehicle = inventory.findByBarcode(reservation.getVehicleBarcode());
        vehicle.setStatus(VehicleStatus.RENTED);

        logService.addLog(vehicle.getBarcode(), VehicleLogType.PICKUP,
                "Picked up by member " + reservation.getMemberId(),
                reservation.getMemberId());

        eventBus.publish(new RentalEvent(
                RentalEventType.PICKUP_REMINDER,
                reservationNumber, reservation.getMemberId(),
                reservation.getVehicleBarcode(), "Vehicle picked up"));

        return reservation;
    }

    /**
     * Completes rental at return branch; applies late fee if overdue.
     *
     * @param reservationNumber reservation id
     * @return updated reservation with late fee on bill if applicable
     */
    public VehicleReservation returnVehicle(String reservationNumber) {
        VehicleReservation reservation = getRequired(reservationNumber);
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Return only allowed from ACTIVE, was: "
                    + reservation.getStatus());
        }

        Instant now = Instant.now();
        reservation.setReturnTime(now);
        reservation.setStatus(ReservationStatus.COMPLETED);

        Vehicle vehicle = inventory.findByBarcode(reservation.getVehicleBarcode());
        vehicle.setBranchId(reservation.getReturnBranchId());
        vehicle.setStatus(VehicleStatus.AVAILABLE);

        LocalDateTime returnDateTime = LocalDateTime.ofInstant(now, java.time.ZoneId.systemDefault());
        if (returnDateTime.isAfter(reservation.getEnd())) {
            long hoursLate = Duration.between(reservation.getEnd(), returnDateTime).toHours();
            if (hoursLate < 1) {
                hoursLate = 1;
            }
            billingService.appendLateFee(reservation.getBill(), hoursLate, vehicle.getLateFeePerHour());

            eventBus.publish(new RentalEvent(
                    RentalEventType.OVERDUE,
                    reservationNumber, reservation.getMemberId(),
                    reservation.getVehicleBarcode(),
                    "Returned " + hoursLate + " hour(s) late"));
        }

        logService.addLog(vehicle.getBarcode(), VehicleLogType.RETURN,
                "Returned at branch " + reservation.getReturnBranchId(),
                reservation.getMemberId());

        eventBus.publish(new RentalEvent(
                RentalEventType.RETURNED,
                reservationNumber, reservation.getMemberId(),
                reservation.getVehicleBarcode(), "Vehicle returned"));

        return reservation;
    }

    /**
     * Searches vehicles available for type/branch and date range.
     * <p>
     * Logic: candidate vehicles must not have CONFIRMED/ACTIVE overlap and
     * must not be in MAINTENANCE.
     *
     * @param type     optional type filter
     * @param branchId pickup branch
     * @param start    window start
     * @param end      window end
     * @return available vehicles
     */
    public List<Vehicle> searchAvailable(VehicleType type, String branchId,
                                         LocalDateTime start, LocalDateTime end) {
        return inventory.search(type, branchId).stream()
                .filter(v -> v.getStatus() != VehicleStatus.MAINTENANCE)
                .filter(v -> !hasOverlappingReservation(v.getBarcode(), start, end, null))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @param memberId member id
     * @return all reservations for member
     */
    public List<VehicleReservation> getReservationsByMember(String memberId) {
        return reservations.values().stream()
                .filter(r -> r.getMemberId().equals(memberId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @param vehicleBarcode vehicle barcode
     * @return active reservation if vehicle is rented, else null
     */
    public VehicleReservation getActiveReservationForVehicle(String vehicleBarcode) {
        return reservations.values().stream()
                .filter(r -> r.getVehicleBarcode().equals(vehicleBarcode))
                .filter(r -> r.getStatus() == ReservationStatus.ACTIVE)
                .findFirst()
                .orElse(null);
    }

    /**
     * @param reservationNumber reservation id
     * @return reservation or throws
     */
    public VehicleReservation getReservation(String reservationNumber) {
        return getRequired(reservationNumber);
    }

    /** Exposes internal map for demo/testing. */
    public Map<String, VehicleReservation> getReservations() {
        return reservations;
    }

    private VehicleReservation getRequired(String reservationNumber) {
        VehicleReservation reservation = reservations.get(reservationNumber);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found: " + reservationNumber);
        }
        return reservation;
    }

    private boolean hasOverlappingReservation(String vehicleBarcode,
                                              LocalDateTime start, LocalDateTime end,
                                              String excludeReservationNumber) {
        return reservations.values().stream()
                .filter(r -> r.getVehicleBarcode().equals(vehicleBarcode))
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED
                        || r.getStatus() == ReservationStatus.ACTIVE)
                .filter(r -> excludeReservationNumber == null
                        || !r.getReservationNumber().equals(excludeReservationNumber))
                .anyMatch(r -> r.overlaps(start, end));
    }
}
