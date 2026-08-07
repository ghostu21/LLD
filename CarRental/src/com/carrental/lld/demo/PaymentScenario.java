package com.carrental.lld.demo;

import com.carrental.lld.billing.PaymentStatus;
import com.carrental.lld.payment.PaymentMethod;
import com.carrental.lld.payment.PaymentRequest;
import com.carrental.lld.payment.PaymentResult;
import com.carrental.lld.reservation.VehicleReservation;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates async payment with retry and exponential backoff.
 */
public class PaymentScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Async payment with retries ---");
        fx.cardGateway.reset();

        LocalDateTime start = LocalDateTime.now().plusDays(30).withHour(12).withMinute(0);
        LocalDateTime end = start.plusDays(2);

        VehicleReservation reservation = fx.reservationService.reserve(
                fx.bob.getId(), fx.sedan.getBarcode(),
                start, end,
                fx.downtown.getBranchId(), fx.downtown.getBranchId(),
                List.of(), Collections.emptyList());

        PaymentRequest request = new PaymentRequest(
                reservation.getReservationNumber(),
                fx.bob.getId(),
                reservation.getBill().getTotal(),
                PaymentMethod.CARD);

        System.out.println("Charging $" + String.format("%.2f", request.getAmount()) + " via CARD...");
        CompletableFuture<PaymentResult> future = fx.paymentService.processPaymentAsync(request);
        PaymentResult result = future.get(10, TimeUnit.SECONDS);

        if (result.isSuccess()) {
            reservation.getBill().setStatus(PaymentStatus.PAID);
            System.out.println("Payment succeeded: " + result.getTransactionId());
        } else {
            reservation.getBill().setStatus(PaymentStatus.FAILED);
            System.out.println("Payment failed: " + result.getMessage());
        }
    }
}
