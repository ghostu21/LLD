package com.hotel.lld.demo;

import com.hotel.lld.booking.RoomBooking;
import com.hotel.lld.payment.PaymentMethod;
import com.hotel.lld.payment.PaymentRequest;
import com.hotel.lld.payment.PaymentResult;
import com.hotel.lld.service.ChargeType;
import com.hotel.lld.service.ServiceCharge;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Demonstrates service charges + credit card / check / cash payment. */
public class PaymentScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Payment & service charges ---");
        LocalDate checkIn = LocalDate.now().plusDays(5);
        RoomBooking booking = fx.bookingService.book(
                fx.alice.getGuestId(), fx.deluxe201.getRoomNumber(), checkIn, 2);

        fx.bookingService.addServiceCharge(booking.getReservationNumber(),
                new ServiceCharge("CH-FOOD-1", ChargeType.FOOD, "Club sandwich", 18.50));
        fx.bookingService.addServiceCharge(booking.getReservationNumber(),
                fx.lateCheckout.toCharge("CH-AM-1"));

        System.out.println("Bill total after charges: $"
                + String.format("%.2f", booking.getBill().getTotal()));
        booking.getBill().getItems().forEach(i ->
                System.out.println("  " + i.getType() + ": " + i.getDescription()
                        + " $" + String.format("%.2f", i.getAmount())));

        PaymentRequest req = new PaymentRequest(
                "PAY-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
                booking.getReservationNumber(),
                booking.getBill().getTotal(),
                PaymentMethod.CREDIT_CARD);
        PaymentResult result = fx.paymentService.processPaymentAsync(req).get();
        System.out.println("Payment: " + result.getStatus() + " — " + result.getMessage());
        if (result.isSuccess()) {
            booking.getBill().setPaid(true);
        }
        TimeUnit.MILLISECONDS.sleep(200);
    }
}
