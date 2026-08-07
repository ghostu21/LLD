package com.carrental.lld.reservation;

import com.carrental.lld.addon.ReservationAddon;
import com.carrental.lld.billing.Bill;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rental reservation aggregate linking member, vehicle, branches, and add-ons.
 * <p>
 * Why: pickup/return may occur at different branches (one-way); add-ons and
 * bill are snapshotted on the reservation for audit and billing.
 */
public class VehicleReservation {
    private final String reservationNumber;
    private final String memberId;
    private final String vehicleBarcode;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String pickupBranchId;
    private final String returnBranchId;
    private ReservationStatus status;
    private final List<ReservationAddon> addons;
    private final List<String> additionalDrivers;
    private Bill bill;
    private Instant pickupTime;
    private Instant returnTime;

    /**
     * @param reservationNumber unique reservation id
     * @param memberId          renting member
     * @param vehicleBarcode    reserved vehicle
     * @param start             rental start
     * @param end               rental end (due)
     * @param pickupBranchId    pickup location
     * @param returnBranchId    return location (may differ for one-way)
     * @param addons            snapshotted add-ons
     * @param additionalDrivers extra authorized drivers
     */
    public VehicleReservation(String reservationNumber, String memberId, String vehicleBarcode,
                              LocalDateTime start, LocalDateTime end,
                              String pickupBranchId, String returnBranchId,
                              List<ReservationAddon> addons, List<String> additionalDrivers) {
        this.reservationNumber = reservationNumber;
        this.memberId = memberId;
        this.vehicleBarcode = vehicleBarcode;
        this.start = start;
        this.end = end;
        this.pickupBranchId = pickupBranchId;
        this.returnBranchId = returnBranchId;
        this.status = ReservationStatus.CONFIRMED;
        this.addons = new ArrayList<>(addons);
        this.additionalDrivers = new ArrayList<>(additionalDrivers);
    }

    /** @return reservation number */
    public String getReservationNumber() { return reservationNumber; }

    /** @return member id */
    public String getMemberId() { return memberId; }

    /** @return vehicle barcode */
    public String getVehicleBarcode() { return vehicleBarcode; }

    /** @return scheduled start */
    public LocalDateTime getStart() { return start; }

    /** @return scheduled end */
    public LocalDateTime getEnd() { return end; }

    /** @return pickup branch */
    public String getPickupBranchId() { return pickupBranchId; }

    /** @return return branch */
    public String getReturnBranchId() { return returnBranchId; }

    /** @return current status */
    public ReservationStatus getStatus() { return status; }

    /** @param status new status */
    public void setStatus(ReservationStatus status) { this.status = status; }

    /** @return snapshotted add-ons */
    public List<ReservationAddon> getAddons() {
        return Collections.unmodifiableList(addons);
    }

    /** @return additional driver ids */
    public List<String> getAdditionalDrivers() {
        return Collections.unmodifiableList(additionalDrivers);
    }

    /** @return linked bill */
    public Bill getBill() { return bill; }

    /** @param bill generated or updated bill */
    public void setBill(Bill bill) { this.bill = bill; }

    /** @return actual pickup instant */
    public Instant getPickupTime() { return pickupTime; }

    /** @param pickupTime pickup timestamp */
    public void setPickupTime(Instant pickupTime) { this.pickupTime = pickupTime; }

    /** @return actual return instant */
    public Instant getReturnTime() { return returnTime; }

    /** @param returnTime return timestamp */
    public void setReturnTime(Instant returnTime) { this.returnTime = returnTime; }

    /**
     * Whether this reservation overlaps another time window.
     * <p>
     * Logic: half-open style overlap — ranges intersect if start &lt; other.end
     * and end &gt; other.start.
     *
     * @param otherStart window start
     * @param otherEnd   window end
     * @return true if overlapping
     */
    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return start.isBefore(otherEnd) && end.isAfter(otherStart);
    }
}
