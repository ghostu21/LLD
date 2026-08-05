package com.amazon.lld.demo;

import com.amazon.lld.cart.Item;
import com.amazon.lld.order.Order;
import com.amazon.lld.payment.PaymentMethodType;
import com.amazon.lld.returns.ReturnReason;
import com.amazon.lld.shipping.ShipmentStatus;

/**
 * Demo: return request → approve → refund with REFUND_APPLIED order status.
 */
public class ReturnsScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Returns & Refunds ---");
        var cart = fx.alice.getCart();
        cart.clear();
        cart.addItem(new Item(fx.phone.getId(), 1, fx.phone.getPrice()), cart.getVersion());

        Order order = fx.checkoutService.checkout(fx.alice, PaymentMethodType.BANK_TRANSFER,
                fx.alice.getAccount().getShippingAddress());
        fx.orderService.ship(order.getOrderId(), "FEDEX123456789");
        order.updateShipmentStatus(ShipmentStatus.DELIVERED);

        var returnReq = fx.returnService.requestReturn(order, ReturnReason.CHANGED_MIND);
        fx.returnService.approve(returnReq.getReturnId());
        var refund = fx.returnService.completeRefund(returnReq.getReturnId(), order);

        System.out.println("Return: " + returnReq.getStatus());
        System.out.println("Order: " + order.getStatus());
        System.out.println("Refund: $" + refund.getAmount() + " " + refund.getStatus());
        Thread.sleep(200);
        cart.clear();
    }
}
