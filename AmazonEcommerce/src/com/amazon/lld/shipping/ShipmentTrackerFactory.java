package com.amazon.lld.shipping;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves {@link ShipmentTracker} by carrier name.
 * <p>
 * Why: orders store carrier + tracking — poller asks factory for the right API client.
 */
public class ShipmentTrackerFactory {
    private static final Map<String, ShipmentTracker> TRACKERS = new HashMap<>();

    static {
        TRACKERS.put("UPS", new UpsShipmentTracker());
        TRACKERS.put("FEDEX", new FedExShipmentTracker());
    }

    /**
     * @param carrier carrier code (UPS, FEDEX)
     * @return tracker implementation
     */
    public static ShipmentTracker get(String carrier) {
        ShipmentTracker tracker = TRACKERS.get(carrier.toUpperCase());
        if (tracker == null) {
            throw new IllegalArgumentException("Unknown carrier: " + carrier);
        }
        return tracker;
    }
}
