package com.amazon.lld.demo;

import com.amazon.lld.events.OrderEvent;
import com.amazon.lld.events.OrderEventType;

/**
 * Demo: async event bus fan-out to NotificationService.
 * <p>
 * Interview angle: AsyncEventBus, not sync SystemNotifier.
 */
public class NotifyScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Async Notifications ---");
        fx.eventBus.publish(new OrderEvent(
                OrderEventType.ORDER_PLACED, "demo-order-1",
                fx.alice.getMemberId(), "Demo order for $42.00"));
        fx.eventBus.publish(new OrderEvent(
                OrderEventType.SHIPMENT_UPDATED, "demo-order-1",
                fx.alice.getMemberId(), "Package IN_TRANSIT"));
        Thread.sleep(300);
    }
}
