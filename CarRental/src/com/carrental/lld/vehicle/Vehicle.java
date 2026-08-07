package com.carrental.lld.vehicle;

/**
 * Rentable vehicle unit with pricing and branch assignment.
 * <p>
 * Why: each physical unit is tracked by barcode with its own stall, rates, and
 * status so reservations can lock a specific vehicle atomically.
 */
public class Vehicle {
    private final String barcode;
    private final VehicleType type;
    private final String make;
    private final String model;
    private String parkingStall;
    private String branchId;
    private final double dailyRate;
    private final double lateFeePerHour;
    private VehicleStatus status;
    private long version;

    /**
     * @param barcode        unique unit barcode
     * @param type           vehicle category
     * @param make           manufacturer
     * @param model          model name
     * @param parkingStall   stall number at branch
     * @param branchId       home branch id
     * @param dailyRate      base daily rental rate
     * @param lateFeePerHour hourly late-return penalty
     */
    public Vehicle(String barcode, VehicleType type, String make, String model,
                   String parkingStall, String branchId, double dailyRate, double lateFeePerHour) {
        this.barcode = barcode;
        this.type = type;
        this.make = make;
        this.model = model;
        this.parkingStall = parkingStall;
        this.branchId = branchId;
        this.dailyRate = dailyRate;
        this.lateFeePerHour = lateFeePerHour;
        this.status = VehicleStatus.AVAILABLE;
        this.version = 0L;
    }

    /** @return unique barcode */
    public String getBarcode() { return barcode; }

    /** @return vehicle type */
    public VehicleType getType() { return type; }

    /** @return manufacturer */
    public String getMake() { return make; }

    /** @return model */
    public String getModel() { return model; }

    /** @return parking stall */
    public String getParkingStall() { return parkingStall; }

    /** @param parkingStall new stall assignment */
    public void setParkingStall(String parkingStall) { this.parkingStall = parkingStall; }

    /** @return current branch id */
    public String getBranchId() { return branchId; }

    /**
     * Moves vehicle to another branch (e.g. one-way return).
     *
     * @param branchId destination branch
     */
    public void setBranchId(String branchId) { this.branchId = branchId; }

    /** @return daily rental rate */
    public double getDailyRate() { return dailyRate; }

    /** @return late fee per overdue hour */
    public double getLateFeePerHour() { return lateFeePerHour; }

    /** @return current status */
    public VehicleStatus getStatus() { return status; }

    /** @param status new lifecycle status */
    public void setStatus(VehicleStatus status) {
        this.status = status;
        this.version++;
    }

    /** @return optimistic-lock version (incremented on status change) */
    public long getVersion() { return version; }

    @Override
    public String toString() {
        return make + " " + model + " [" + barcode + ", " + type + ", " + status + "]";
    }
}
