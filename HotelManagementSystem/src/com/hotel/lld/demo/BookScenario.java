package com.hotel.lld.demo;

import com.hotel.lld.booking.RoomBooking;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/** Demonstrates search → lock → book → mark reserved flow. */
public class BookScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Book room ---");
        LocalDate checkIn = LocalDate.now().plusDays(14);
        RoomBooking booking = fx.bookingService.book(
                fx.alice.getGuestId(), fx.deluxe201.getRoomNumber(), checkIn, 3);
        System.out.println("Booked " + booking.getReservationNumber()
                + " room=" + booking.getRoomNumber()
                + " status=" + booking.getStatus()
                + " bill=$" + String.format("%.2f", booking.getBill().getTotal()));
        TimeUnit.MILLISECONDS.sleep(200);
    }
}
