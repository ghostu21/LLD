package com.amazon.lld.shipping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FedEx shipment tracker stub with progressing statuses.
 * <p>
 * Why: mirrors {@link UpsShipmentTracker} for a second carrier — factory
 * selects implementation by carrier name.
 */
public class FedExShipmentTracker implements ShipmentTracker {
    private static final ShipmentStatus[] PROGRESSION = {
            ShipmentStatus.LABEL_CREATED,
            ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.OUT_FOR_DELIVERY,
            ShipmentStatus.DELIVERED
    };

    private final Map<String, Integer> pollCounts = new ConcurrentHashMap<>();

    /**
     * Returns next FedEx-simulated status for the tracking number.
     */
    @Override
    public ShipmentStatus getStatus(String trackingNumber) {
        int count = pollCounts.merge(trackingNumber, 1, Integer::sum) - 1;
        int idx = Math.min(count, PROGRESSION.length - 1);
        return PROGRESSION[idx];
    }
}
