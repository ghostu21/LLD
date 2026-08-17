package com.hotel.lld.demo;

import com.hotel.lld.booking.RoomBooking;
import com.hotel.lld.booking.RoomNotAvailableException;

import java.time.LocalDate;

/** Demonstrates overlapping date rejection on the same room. */
public class OverlapScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Overlap rejection ---");
        LocalDate start = LocalDate.now().plusDays(20);
        RoomBooking first = fx.bookingService.book(
                fx.alice.getGuestId(), fx.suite301.getRoomNumber(), start, 3);
        System.out.println("First OK: " + first.getReservationNumber());

        try {
            fx.bookingService.book(
                    fx.bob.getGuestId(), fx.suite301.getRoomNumber(), start.plusDays(1), 2);
            System.out.println("ERROR: overlap should have failed");
        } catch (RoomNotAvailableException e) {
            System.out.println("Expected failure: " + e.getMessage());
        }
    }
}
