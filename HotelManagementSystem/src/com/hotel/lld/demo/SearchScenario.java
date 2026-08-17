package com.hotel.lld.demo;

import com.hotel.lld.room.Room;
import com.hotel.lld.room.RoomStyle;

import java.time.LocalDate;
import java.util.List;

/** Demonstrates date-based inventory search. */
public class SearchScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Search rooms (date-based calendar) ---");
        LocalDate start = LocalDate.now().plusDays(7);
        List<Room> deluxe = fx.bookingService.search(RoomStyle.DELUXE, start, 2);
        System.out.println("DELUXE available from " + start + " for 2 nights: " + deluxe.size());
        for (Room r : deluxe) {
            System.out.println("  room " + r.getRoomNumber() + " @ $" + r.getBookingPrice() + "/night");
        }
    }
}
