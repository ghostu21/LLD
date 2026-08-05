package com.amazon.lld.order;

import com.amazon.lld.events.AsyncEventBus;
import com.amazon.lld.events.OrderEvent;
import com.amazon.lld.events.OrderEventType;
import com.amazon.lld.inventory.InventoryService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Order lifecycle helpers: cancel and ship with event publishing.
 * <p>
 * Why: separates post-checkout mutations from checkout itself; each action
 * publishes the appropriate domain event.
 */
public class OrderService {
    private final Map<String, Order> orders;
    private final InventoryService inventoryService;
    private final AsyncEventBus eventBus;

    /**
     * @param orders            order store (shared with checkout)
     * @param inventoryService  releases stock on cancel
     * @param eventBus          async notifications
     */
    public OrderService(Map<String, Order> orders,
                      InventoryService inventoryService,
                      AsyncEventBus eventBus) {
        this.orders = orders != null ? orders : new ConcurrentHashMap<>();
        this.inventoryService = inventoryService;
        this.eventBus = eventBus;
    }

    /**
     * Cancels an order if not yet shipped; releases inventory.
     *
     * @param orderId order to cancel
     */
    public void cancel(String orderId) {
        Order order = requireOrder(orderId);
        order.cancel();
        for (var item : order.getItems()) {
            inventoryService.release(item.getProductId(), item.getQuantity());
        }
        publish(OrderEventType.ORDER_CANCELED, order, "Canceled before shipment");
    }

    /**
     * Marks order shipped with tracking number.
     *
     * @param orderId        order id
     * @param trackingNumber carrier tracking
     */
    public void ship(String orderId, String trackingNumber) {
        Order order = requireOrder(orderId);
        order.markShipped(trackingNumber);
        publish(OrderEventType.ORDER_SHIPPED, order,
                "Tracking: " + trackingNumber);
    }

    private Order requireOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) throw new IllegalArgumentException("Order not found: " + orderId);
        return order;
    }

    private void publish(OrderEventType type, Order order, String payload) {
        if (eventBus != null) {
            eventBus.publish(new OrderEvent(type, order.getOrderId(),
                    order.getMemberId(), payload));
        }
    }
}
