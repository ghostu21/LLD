package com.carrental.lld.demo;

import com.carrental.lld.reservation.VehicleReservation;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Demonstrates StandardCancellationPolicy fee tiers.
 */
public class CancelScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Cancellation policy ---");

        LocalDateTime farStart = LocalDateTime.now().plusDays(60).withHour(10).withMinute(0);
        VehicleReservation far = fx.reservationService.reserve(
                fx.alice.getId(), fx.sedan.getBarcode(),
                farStart, farStart.plusDays(2),
                fx.downtown.getBranchId(), fx.downtown.getBranchId(),
                List.of(), Collections.emptyList());
        double totalFar = far.getBill().getTotal();
        fx.reservationService.cancel(far.getReservationNumber());
        double feeFar = far.getBill().getItems().stream()
                .filter(i -> i.getDescription().contains("Cancellation"))
                .mapToDouble(i -> i.getAmount()).sum();
        System.out.println(">48h notice: bill=$" + String.format("%.2f", totalFar)
                + " cancellation fee=$" + String.format("%.2f", feeFar));

        LocalDateTime soonStart = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        VehicleReservation soon = fx.reservationService.reserve(
                fx.bob.getId(), fx.truck.getBarcode(),
                soonStart, soonStart.plusDays(1),
                fx.airport.getBranchId(), fx.airport.getBranchId(),
                List.of(), Collections.emptyList());
        double totalSoon = soon.getBill().getTotal();
        fx.reservationService.cancel(soon.getReservationNumber());
        double feeSoon = soon.getBill().getItems().stream()
                .filter(i -> i.getDescription().contains("Cancellation"))
                .mapToDouble(i -> i.getAmount()).sum();
        System.out.println("<24h notice: bill=$" + String.format("%.2f", totalSoon)
                + " cancellation fee=$" + String.format("%.2f", feeSoon)
                + " (50%)");

        java.util.concurrent.TimeUnit.MILLISECONDS.sleep(200);
    }
}
