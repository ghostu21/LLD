package com.hotel.lld.demo;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/** Demonstrates async event-driven notifications (email / SMS / push). */
public class NotifyScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Event-driven notifications ---");
        fx.bookingService.book(
                fx.bob.getGuestId(), fx.suite301.getRoomNumber(),
                LocalDate.now().plusDays(40), 2);

        fx.bookingService.publishReminders(LocalDate.now().plusDays(39));
        TimeUnit.MILLISECONDS.sleep(300);
        System.out.println("(email / sms / push listeners printed above)");
    }
}
