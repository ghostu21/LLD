package com.hotel.lld.booking;

import com.hotel.lld.billing.Bill;
import com.hotel.lld.billing.BillingService;
import com.hotel.lld.events.AsyncEventBus;
import com.hotel.lld.events.HotelEvent;
import com.hotel.lld.events.HotelEventType;
import com.hotel.lld.room.Room;
import com.hotel.lld.room.RoomInventory;
import com.hotel.lld.room.RoomStatus;
import com.hotel.lld.room.RoomStyle;
import com.hotel.lld.service.HousekeepingWorkflow;
import com.hotel.lld.service.ServiceCharge;

import java.time.LocalDate;
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
 * Booking orchestrator: search → lock room → validate calendar → create booking → mark reserved.
 * <p>
 * Why: per-room {@link ReentrantLock} prevents double-booking under concurrency;
 * availability is date-based via {@link Room#isAvailable}.
 * <p>
 * Interview takeaway: booking systems fail not when traffic spikes — but when locks are missing.
 */
public class BookingService {
    private static final long LOCK_TIMEOUT_SECONDS = 5L;

    private final RoomInventory inventory;
    private final BillingService billingService;
    private final AsyncEventBus eventBus;
    private final CancellationPolicy cancellationPolicy;
    private final HousekeepingWorkflow housekeepingWorkflow;

    private final Map<String, RoomBooking> bookings = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> roomLocks = new ConcurrentHashMap<>();

    public BookingService(RoomInventory inventory, BillingService billingService,
                          AsyncEventBus eventBus, CancellationPolicy cancellationPolicy,
                          HousekeepingWorkflow housekeepingWorkflow) {
        this.inventory = inventory;
        this.billingService = billingService;
        this.eventBus = eventBus;
        this.cancellationPolicy = cancellationPolicy;
        this.housekeepingWorkflow = housekeepingWorkflow;
    }

    public List<Room> search(RoomStyle style, LocalDate start, int nights) {
        return inventory.search(style, start, nights);
    }

    /**
     * Books a room under per-room lock.
     * Flow: Search → Lock Room → Validate Availability → Create Booking → Mark Dates Reserved
     */
    public RoomBooking book(String guestId, String roomNumber, LocalDate checkIn, int nights) {
        if (nights <= 0) {
            throw new IllegalArgumentException("nights must be positive");
        }

        ReentrantLock lock = roomLocks.computeIfAbsent(roomNumber, k -> new ReentrantLock());
        boolean acquired = false;
        try {
            acquired = lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BookingTimeoutException(roomNumber);
            }

            Room room = inventory.findByNumber(roomNumber);
            if (!room.isAvailable(checkIn, nights)) {
                throw new RoomNotAvailableException(roomNumber, "dates not free on calendar");
            }

            String reservationNumber = "HTL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            RoomBooking booking = new RoomBooking(reservationNumber, guestId, roomNumber, checkIn, nights);

            Bill bill = billingService.generateBill(booking, room);
            room.markReserved(checkIn, nights);
            room.setStatus(RoomStatus.RESERVED);
            booking.setStatus(BookingStatus.CONFIRMED);
            bookings.put(reservationNumber, booking);

            eventBus.publish(new HotelEvent(
                    HotelEventType.BOOKING_CONFIRMED,
                    reservationNumber, guestId, roomNumber,
                    "Confirmed " + checkIn + " for " + nights + " night(s)"));

            return booking;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BookingTimeoutException(roomNumber);
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }

    /**
     * Cancel → Validate Policy → Initiate Refund → Update Booking + Payment Status.
     */
    public Refund cancel(String reservationNumber, LocalDateTime cancelAt) {
        RoomBooking booking = requireBooking(reservationNumber);
        if (booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Cannot cancel booking in status " + booking.getStatus());
        }

        ReentrantLock lock = roomLocks.computeIfAbsent(booking.getRoomNumber(), k -> new ReentrantLock());
        boolean acquired = false;
        try {
            acquired = lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BookingTimeoutException(booking.getRoomNumber());
            }

            Refund refund = cancellationPolicy.calculateRefund(booking, cancelAt);
            billingService.applyRefund(booking.getBill(), refund);

            Room room = inventory.findByNumber(booking.getRoomNumber());
            room.markAvailable(booking.getCheckIn(), booking.getDurationInDays());
            if (room.getStatus() == RoomStatus.RESERVED || room.getStatus() == RoomStatus.OCCUPIED) {
                room.setStatus(RoomStatus.AVAILABLE);
            }

            booking.setStatus(BookingStatus.CANCELLED);
            eventBus.publish(new HotelEvent(
                    HotelEventType.BOOKING_CANCELLED,
                    reservationNumber, booking.getGuestId(), booking.getRoomNumber(),
                    refund.toString()));

            return refund;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BookingTimeoutException(booking.getRoomNumber());
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }

    public void checkIn(String reservationNumber) {
        RoomBooking booking = requireBooking(reservationNumber);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Check-in requires CONFIRMED status");
        }
        Room room = inventory.findByNumber(booking.getRoomNumber());
        room.checkIn();
        booking.setActualCheckIn(LocalDateTime.now());
        booking.setStatus(BookingStatus.CHECKED_IN);
        eventBus.publish(new HotelEvent(
                HotelEventType.CHECKED_IN,
                reservationNumber, booking.getGuestId(), booking.getRoomNumber(),
                "Guest checked in"));
    }

    /**
     * Checkout → BEING_SERVICED → housekeeping workflow → AVAILABLE.
     */
    public void checkOut(String reservationNumber) {
        RoomBooking booking = requireBooking(reservationNumber);
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Check-out requires CHECKED_IN status");
        }
        Room room = inventory.findByNumber(booking.getRoomNumber());
        room.checkOut();
        booking.setActualCheckOut(LocalDateTime.now());
        booking.setStatus(BookingStatus.CHECKED_OUT);
        billingService.refreshWithCharges(booking);

        housekeepingWorkflow.assignAfterCheckout(room.getRoomNumber(), "STAFF-HK-01");

        eventBus.publish(new HotelEvent(
                HotelEventType.CHECKED_OUT,
                reservationNumber, booking.getGuestId(), booking.getRoomNumber(),
                "Guest checked out — housekeeping assigned"));
    }

    public void addServiceCharge(String reservationNumber, ServiceCharge charge) {
        RoomBooking booking = requireBooking(reservationNumber);
        if (booking.getStatus() != BookingStatus.CHECKED_IN
                && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot add charges in status " + booking.getStatus());
        }
        booking.addCharge(charge);
        billingService.appendServiceCharge(booking.getBill(), charge);
    }

    public RoomBooking getBooking(String reservationNumber) {
        return requireBooking(reservationNumber);
    }

    /** Who booked a particular room (active / historical). */
    public List<RoomBooking> bookingsForRoom(String roomNumber) {
        return bookings.values().stream()
                .filter(b -> b.getRoomNumber().equals(roomNumber))
                .collect(Collectors.toList());
    }

    /** What rooms were booked by a specific guest. */
    public List<RoomBooking> bookingsForGuest(String guestId) {
        return bookings.values().stream()
                .filter(b -> b.getGuestId().equals(guestId))
                .collect(Collectors.toList());
    }

    public List<RoomBooking> allBookings() {
        return new ArrayList<>(bookings.values());
    }

    /** Publishes check-in / check-out reminders for demos and schedulers. */
    public void publishReminders(LocalDate today) {
        for (RoomBooking booking : bookings.values()) {
            if (booking.getStatus() != BookingStatus.CONFIRMED
                    && booking.getStatus() != BookingStatus.CHECKED_IN) {
                continue;
            }
            if (booking.getCheckIn().equals(today.plusDays(1))) {
                eventBus.publish(new HotelEvent(
                        HotelEventType.CHECK_IN_REMINDER,
                        booking.getReservationNumber(), booking.getGuestId(),
                        booking.getRoomNumber(), "Check-in tomorrow"));
            }
            if (booking.getCheckOut().equals(today.plusDays(1))
                    && booking.getStatus() == BookingStatus.CHECKED_IN) {
                eventBus.publish(new HotelEvent(
                        HotelEventType.CHECK_OUT_REMINDER,
                        booking.getReservationNumber(), booking.getGuestId(),
                        booking.getRoomNumber(), "Check-out tomorrow"));
            }
        }
    }

    private RoomBooking requireBooking(String reservationNumber) {
        RoomBooking booking = bookings.get(reservationNumber);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + reservationNumber);
        }
        return booking;
    }
}
