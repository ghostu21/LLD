package com.amazon.lld.demo;

import com.amazon.lld.cart.Item;
import com.amazon.lld.order.Order;
import com.amazon.lld.payment.PaymentMethodType;
import com.amazon.lld.shipping.Shipment;
import com.amazon.lld.shipping.ShipmentPoller;
import com.amazon.lld.shipping.ShipmentStatus;

import java.util.List;

/**
 * Demo: UPS/FedEx trackers + ShipmentPoller advancing status.
 * <p>
 * Interview angle: ShipmentTracker interface, not inline carrier if/else.
 */
public class ShippingScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Shipment Tracking + Poller ---");
        var cart = fx.alice.getCart();
        cart.clear();
        cart.addItem(new Item(fx.shirt.getId(), 1, fx.shirt.getPrice()), cart.getVersion());

        Order order = fx.checkoutService.checkout(fx.alice, PaymentMethodType.CREDIT_CARD,
                fx.alice.getAccount().getShippingAddress());
        fx.orderService.ship(order.getOrderId(), "UPS1Z999AA10123456");

        Shipment shipment = new Shipment(order.getOrderId(), "UPS1Z999AA10123456",
                "UPS", ShipmentStatus.LABEL_CREATED);
        ShipmentPoller poller = new ShipmentPoller(fx.eventBus, fx.checkoutService.getOrders());

        for (int i = 0; i < 4; i++) {
            poller.pollOnce(List.of(shipment));
            System.out.println("  Poll " + (i + 1) + ": " + shipment.getStatus());
        }
        Thread.sleep(200);
        cart.clear();
    }
}
