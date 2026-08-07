package com.carrental.lld.events;

import java.time.Instant;

/**
 * Immutable rental domain event for pub/sub.
 * <p>
 * Why: decouples reservation/billing/payment from notification delivery.
 */
public class RentalEvent {
    private final RentalEventType type;
    private final String reservationNumber;
    private final String memberId;
    private final String vehicleBarcode;
    private final String payload;
    private final Instant timestamp;

    /**
     * @param type              event kind
     * @param reservationNumber affected reservation
     * @param memberId          member to notify
     * @param vehicleBarcode    vehicle involved
     * @param payload           human-readable detail
     */
    public RentalEvent(RentalEventType type, String reservationNumber, String memberId,
                       String vehicleBarcode, String payload) {
        this.type = type;
        this.reservationNumber = reservationNumber;
        this.memberId = memberId;
        this.vehicleBarcode = vehicleBarcode;
        this.payload = payload;
        this.timestamp = Instant.now();
    }

    /** @return event type */
    public RentalEventType getType() { return type; }

    /** @return reservation number */
    public String getReservationNumber() { return reservationNumber; }

    /** @return member id */
    public String getMemberId() { return memberId; }

    /** @return vehicle barcode */
    public String getVehicleBarcode() { return vehicleBarcode; }

    /** @return detail payload */
    public String getPayload() { return payload; }

    /** @return creation time */
    public Instant getTimestamp() { return timestamp; }
}
