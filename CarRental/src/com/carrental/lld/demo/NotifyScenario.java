package com.carrental.lld.demo;

import com.carrental.lld.events.RentalEvent;
import com.carrental.lld.events.RentalEventType;

import java.util.concurrent.TimeUnit;

/**
 * Demonstrates async event bus fan-out to NotificationService.
 */
public class NotifyScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Async notifications ---");
        fx.eventBus.publish(new RentalEvent(
                RentalEventType.DUE_REMINDER,
                "RES-DEMO", fx.alice.getId(), fx.sedan.getBarcode(),
                "Return due tomorrow at 10:00 AM"));

        fx.eventBus.publish(new RentalEvent(
                RentalEventType.OVERDUE,
                "RES-DEMO", fx.bob.getId(), fx.truck.getBarcode(),
                "Vehicle is 3 hours overdue"));

        TimeUnit.MILLISECONDS.sleep(300);
        System.out.println("(notifications printed asynchronously above)");
    }
}
