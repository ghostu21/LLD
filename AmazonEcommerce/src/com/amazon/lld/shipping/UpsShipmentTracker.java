package com.amazon.lld.shipping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UPS shipment tracker stub that simulates progressing delivery status.
 * <p>
 * Why: demo poller needs a carrier-specific implementation without real API keys.
 * <p>
 * Logic: each poll advances status along a fixed progression based on call count.
 */
public class UpsShipmentTracker implements ShipmentTracker {
    private static final ShipmentStatus[] PROGRESSION = {
            ShipmentStatus.LABEL_CREATED,
            ShipmentStatus.PICKED_UP,
            ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.OUT_FOR_DELIVERY,
            ShipmentStatus.DELIVERED
    };

    private final Map<String, Integer> pollCounts = new ConcurrentHashMap<>();

    /**
     * Returns next status in progression for the tracking number.
     */
    @Override
    public ShipmentStatus getStatus(String trackingNumber) {
        int count = pollCounts.merge(trackingNumber, 1, Integer::sum) - 1;
        int idx = Math.min(count, PROGRESSION.length - 1);
        return PROGRESSION[idx];
    }
}
