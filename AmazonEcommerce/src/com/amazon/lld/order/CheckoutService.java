package com.amazon.lld.order;

import com.amazon.lld.account.AccessControl;
import com.amazon.lld.account.Address;
import com.amazon.lld.account.Member;
import com.amazon.lld.cart.Item;
import com.amazon.lld.cart.ShoppingCart;
import com.amazon.lld.events.AsyncEventBus;
import com.amazon.lld.events.OrderEvent;
import com.amazon.lld.events.OrderEventType;
import com.amazon.lld.inventory.InventoryService;
import com.amazon.lld.payment.Payment;
import com.amazon.lld.payment.PaymentMethodType;
import com.amazon.lld.payment.PaymentProcessor;
import com.amazon.lld.payment.PaymentStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Checkout orchestration: validate member, pay, reserve stock, create order, clear cart.
 * <p>
 * Why: single transactional boundary for placing an order from a member cart.
 * <p>
 * Logic: access check → inventory reserve → payment → order snapshot → clear cart
 * → publish ORDER_PLACED.
 */
public class CheckoutService {
    private final PaymentProcessor paymentProcessor;
    private final InventoryService inventoryService;
    private final AsyncEventBus eventBus;
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    /**
     * @param paymentProcessor processes payment via strategy
     * @param inventoryService reserves stock
     * @param eventBus         optional async notifications
     */
    public CheckoutService(PaymentProcessor paymentProcessor,
                           InventoryService inventoryService,
                           AsyncEventBus eventBus) {
        this.paymentProcessor = paymentProcessor;
        this.inventoryService = inventoryService;
        this.eventBus = eventBus;
    }

    /**
     * Places an order from the member's cart.
     *
     * @param member     buyer
     * @param methodType payment rail
     * @param address    shipping address (may override profile default)
     * @return created order
     */
    public Order checkout(Member member, PaymentMethodType methodType, Address address) {
        AccessControl.requirePurchase(member);
        ShoppingCart cart = member.getCart();
        List<Item> items = new ArrayList<>(cart.getItems());
        if (items.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        for (Item item : items) {
            if (!inventoryService.reserve(item.getProductId(), item.getQuantity())) {
                throw new IllegalStateException("Insufficient stock for: " + item.getProductId());
            }
        }

        double total = cart.total();
        Payment payment = paymentProcessor.process(total, methodType);
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            for (Item item : items) {
                inventoryService.release(item.getProductId(), item.getQuantity());
            }
            throw new IllegalStateException("Payment failed");
        }

        Order order = new Order(member.getMemberId(), items, address, payment);
        orders.put(order.getOrderId(), order);
        cart.clear();

        if (eventBus != null) {
            eventBus.publish(new OrderEvent(
                    OrderEventType.ORDER_PLACED,
                    order.getOrderId(),
                    member.getMemberId(),
                    total + " charged via " + methodType));
        }
        return order;
    }

    /** @return all orders by id (for demos) */
    public Map<String, Order> getOrders() { return orders; }

    /**
     * @param orderId order id
     * @return order or null
     */
    public Order getOrder(String orderId) { return orders.get(orderId); }
}
