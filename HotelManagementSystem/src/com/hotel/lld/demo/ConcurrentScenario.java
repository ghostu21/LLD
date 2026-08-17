package com.hotel.lld.demo;

import com.hotel.lld.booking.RoomBooking;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates per-room locking under concurrent book attempts.
 */
public class ConcurrentScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Concurrent booking (per-room lock) ---");
        LocalDate start = LocalDate.now().plusDays(60);
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failure = new AtomicInteger(0);

        Runnable attempt = () -> {
            try {
                ready.await();
                RoomBooking booking = fx.bookingService.book(
                        fx.alice.getGuestId(), fx.deluxe201.getRoomNumber(), start, 2);
                success.incrementAndGet();
                System.out.println("Won lock: " + booking.getReservationNumber());
            } catch (Exception e) {
                failure.incrementAndGet();
                System.out.println("Lost race: " + e.getMessage());
            } finally {
                done.countDown();
            }
        };

        Thread t1 = new Thread(attempt, "booker-1");
        Thread t2 = new Thread(attempt, "booker-2");
        t1.start();
        t2.start();
        ready.countDown();
        done.await();

        System.out.println("Result: success=" + success.get() + " failure=" + failure.get()
                + " (expect 1 / 1)");
    }
}
