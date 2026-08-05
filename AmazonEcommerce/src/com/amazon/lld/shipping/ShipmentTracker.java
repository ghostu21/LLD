package com.amazon.lld.shipping;

/**
 * Contract for querying carrier shipment status by tracking number.
 * <p>
 * Why: UPS and FedEx have different APIs — Strategy-like per-carrier
 * implementations behind one interface for {@link ShipmentPoller}.
 */
public interface ShipmentTracker {
    /**
     * Fetches current status for a tracking number.
     *
     * @param trackingNumber carrier tracking id
     * @return latest {@link ShipmentStatus}
     */
    ShipmentStatus getStatus(String trackingNumber);
}
