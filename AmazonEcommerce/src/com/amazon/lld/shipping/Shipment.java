package com.amazon.lld.shipping;

/**
 * Shipment record linking an order to carrier tracking.
 * <p>
 * Why: poller operates on shipments; orders reference tracking via this type.
 */
public class Shipment {
    private final String orderId;
    private final String trackingNumber;
    private final String carrier;
    private ShipmentStatus status;

    /**
     * @param orderId         associated order
     * @param trackingNumber  carrier tracking id
     * @param carrier         UPS or FEDEX
     * @param status          initial status
     */
    public Shipment(String orderId, String trackingNumber, String carrier, ShipmentStatus status) {
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.status = status;
    }

    /** @return order id */
    public String getOrderId() { return orderId; }

    /** @return tracking number */
    public String getTrackingNumber() { return trackingNumber; }

    /** @return carrier code */
    public String getCarrier() { return carrier; }

    /** @return current status */
    public ShipmentStatus getStatus() { return status; }

    /** Updates status after poller fetch. */
    public void setStatus(ShipmentStatus status) { this.status = status; }
}
