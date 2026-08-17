package com.hotel.lld.demo;

import com.hotel.lld.booking.RoomBooking;

import java.time.LocalDate;
import java.util.List;

/** Demonstrates guest ↔ room information retrieval. */
public class GuestQueryScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Guest / room queries ---");
        LocalDate start = LocalDate.now().plusDays(50);
        RoomBooking b1 = fx.bookingService.book(
                fx.alice.getGuestId(), fx.standard101.getRoomNumber(), start, 2);
        RoomBooking b2 = fx.bookingService.book(
                fx.alice.getGuestId(), fx.suite301.getRoomNumber(), start.plusDays(10), 1);

        List<RoomBooking> byGuest = fx.bookingService.bookingsForGuest(fx.alice.getGuestId());
        System.out.println("Alice bookings: " + byGuest.size());
        byGuest.forEach(b -> System.out.println("  " + b.getReservationNumber()
                + " room=" + b.getRoomNumber() + " status=" + b.getStatus()));

        List<RoomBooking> byRoom = fx.bookingService.bookingsForRoom(fx.standard101.getRoomNumber());
        System.out.println("Room 101 history includes: " + b1.getReservationNumber()
                + " (and possibly earlier cancellations)");
        System.out.println("Latest suite booking: " + b2.getReservationNumber());
        System.out.println("Room 101 bookings count: " + byRoom.size());
    }
}
