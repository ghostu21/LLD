package com.amazon.lld.demo;

import com.amazon.lld.account.Address;
import com.amazon.lld.cart.Item;
import com.amazon.lld.command.PlaceOrderCommand;
import com.amazon.lld.payment.PaymentMethodType;

/**
 * Demo: end-to-end checkout with inventory reserve and ORDER_PLACED event.
 */
public class CheckoutScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Checkout ---");
        var cart = fx.alice.getCart();
        cart.clear();
        cart.addItem(new Item(fx.book.getId(), 2, fx.book.getPrice()), cart.getVersion());

        Address addr = fx.alice.getAccount().getShippingAddress();
        PlaceOrderCommand cmd = new PlaceOrderCommand(
                fx.checkoutService, fx.alice, PaymentMethodType.CREDIT_CARD, addr);
        cmd.execute();

        System.out.println("Order placed: " + cmd.getResult().getOrderId());
        System.out.println("Cart cleared, items=" + cart.getItems().size());
        Thread.sleep(200);
    }
}
