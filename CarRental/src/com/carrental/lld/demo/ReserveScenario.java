package com.carrental.lld.demo;

import com.carrental.lld.reservation.VehicleReservation;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Demonstrates concurrent-safe reservation under per-vehicle lock.
 */
public class ReserveScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Reserve (lock-protected) ---");
        LocalDateTime start = LocalDateTime.now().plusDays(5).withHour(9).withMinute(0);
        LocalDateTime end = start.plusDays(2);

        VehicleReservation reservation = fx.reservationService.reserve(
                fx.alice.getId(), fx.sedan.getBarcode(),
                start, end,
                fx.downtown.getBranchId(), fx.downtown.getBranchId(),
                List.of(), Collections.emptyList());

        System.out.println("Reserved: " + reservation.getReservationNumber()
                + " status=" + reservation.getStatus());
        System.out.println("Bill total: $" + String.format("%.2f", reservation.getBill().getTotal()));
        System.out.println("Vehicle status: " + fx.sedan.getStatus());
    }
}
