package com.carrental.lld.log;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable audit entry for a vehicle event.
 */
public class VehicleLog {
    private final String logId;
    private final String vehicleBarcode;
    private final VehicleLogType type;
    private final String description;
    private final Instant timestamp;
    private final String performedBy;

    /**
     * @param vehicleBarcode affected vehicle
     * @param type           log category
     * @param description    detail text
     * @param performedBy    actor (member id or system)
     */
    public VehicleLog(String vehicleBarcode, VehicleLogType type,
                      String description, String performedBy) {
        this.logId = UUID.randomUUID().toString();
        this.vehicleBarcode = vehicleBarcode;
        this.type = type;
        this.description = description;
        this.timestamp = Instant.now();
        this.performedBy = performedBy;
    }

    /** @return log id */
    public String getLogId() { return logId; }

    /** @return vehicle barcode */
    public String getVehicleBarcode() { return vehicleBarcode; }

    /** @return log type */
    public VehicleLogType getType() { return type; }

    /** @return description */
    public String getDescription() { return description; }

    /** @return when logged */
    public Instant getTimestamp() { return timestamp; }

    /** @return actor */
    public String getPerformedBy() { return performedBy; }

    @Override
    public String toString() {
        return "[" + type + "] " + vehicleBarcode + " @ " + timestamp + ": " + description;
    }
}
