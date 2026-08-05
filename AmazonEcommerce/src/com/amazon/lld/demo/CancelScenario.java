package com.amazon.lld.demo;

import com.amazon.lld.cart.Item;
import com.amazon.lld.order.Order;
import com.amazon.lld.payment.PaymentMethodType;

/**
 * Demo: cancel order only before shipment; blocked after ship.
 */
public class CancelScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Order Cancel ---");
        var cart = fx.alice.getCart();
        cart.clear();
        cart.addItem(new Item(fx.book.getId(), 1, fx.book.getPrice()), cart.getVersion());

        Order order = fx.checkoutService.checkout(fx.alice, PaymentMethodType.CREDIT_CARD,
                fx.alice.getAccount().getShippingAddress());
        fx.orderService.cancel(order.getOrderId());
        System.out.println("Canceled UNSHIPPED order: " + order.getStatus());

        cart.clear();
        cart.addItem(new Item(fx.shirt.getId(), 1, fx.shirt.getPrice()), cart.getVersion());
        Order shipped = fx.checkoutService.checkout(fx.alice, PaymentMethodType.CREDIT_CARD,
                fx.alice.getAccount().getShippingAddress());
        fx.orderService.ship(shipped.getOrderId(), "UPS-CANCEL-TEST");

        try {
            shipped.cancel();
        } catch (IllegalStateException e) {
            System.out.println("Cannot cancel SHIPPED: " + e.getMessage());
        }
        System.out.println("Shipped order status: " + shipped.getStatus());
        Thread.sleep(200);
        cart.clear();
    }
}
