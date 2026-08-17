package com.hotel.lld.demo;

import com.hotel.lld.booking.Refund;
import com.hotel.lld.booking.RoomBooking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/** Demonstrates FullRefundBefore24HoursPolicy. */
public class CancelScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Cancellation & refund policy ---");

        LocalDate farCheckIn = LocalDate.now().plusDays(30);
        RoomBooking far = fx.bookingService.book(
                fx.alice.getGuestId(), fx.standard101.getRoomNumber(), farCheckIn, 2);
        double totalFar = far.getBill().getTotal();
        Refund refundFar = fx.bookingService.cancel(far.getReservationNumber(), LocalDateTime.now());
        System.out.println(">24h notice: bill=$" + String.format("%.2f", totalFar)
                + " → " + refundFar);

        LocalDate soonCheckIn = LocalDate.now().plusDays(1);
        RoomBooking soon = fx.bookingService.book(
                fx.bob.getGuestId(), fx.standard101.getRoomNumber(), soonCheckIn, 1);
        double totalSoon = soon.getBill().getTotal();
        Refund refundSoon = fx.bookingService.cancel(soon.getReservationNumber(), LocalDateTime.now());
        System.out.println("<24h notice: bill=$" + String.format("%.2f", totalSoon)
                + " → " + refundSoon);

        TimeUnit.MILLISECONDS.sleep(200);
    }
}
