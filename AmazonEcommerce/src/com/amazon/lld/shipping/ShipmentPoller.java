package com.amazon.lld.shipping;

import com.amazon.lld.events.AsyncEventBus;
import com.amazon.lld.events.OrderEvent;
import com.amazon.lld.events.OrderEventType;
import com.amazon.lld.order.Order;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Polls carrier APIs for shipped orders and publishes status updates.
 * <p>
 * Why: shipment status changes asynchronously — a scheduled poller (or manual
 * {@link #pollOnce}) keeps orders in sync without blocking checkout.
 * <p>
 * Logic: for each shipment, call tracker.getStatus → if changed, update order
 * and publish SHIPMENT_UPDATED on the optional event bus.
 */
public class ShipmentPoller {
    private final AsyncEventBus eventBus;
    private final Map<String, Order> ordersById;

    /**
     * @param eventBus optional bus for SHIPMENT_UPDATED (may be null)
     * @param ordersById order lookup by id
     */
    public ShipmentPoller(AsyncEventBus eventBus, Map<String, Order> ordersById) {
        this.eventBus = eventBus;
        this.ordersById = ordersById != null ? ordersById : new ConcurrentHashMap<>();
    }

    /**
     * One polling cycle over the given shipments.
     *
     * @param shipments active shipments to poll
     */
    public void pollOnce(List<Shipment> shipments) {
        for (Shipment shipment : shipments) {
            ShipmentTracker tracker = ShipmentTrackerFactory.get(shipment.getCarrier());
            ShipmentStatus newStatus = tracker.getStatus(shipment.getTrackingNumber());
            if (newStatus != shipment.getStatus()) {
                shipment.setStatus(newStatus);
                Order order = ordersById.get(shipment.getOrderId());
                if (order != null) {
                    order.updateShipmentStatus(newStatus);
                    if (eventBus != null) {
                        eventBus.publish(new OrderEvent(
                                OrderEventType.SHIPMENT_UPDATED,
                                order.getOrderId(),
                                order.getMemberId(),
                                "Shipment " + newStatus + " (" + shipment.getTrackingNumber() + ")"));
                    }
                }
            }
        }
    }
}
