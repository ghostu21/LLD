package com.carrental.lld.demo;

import com.carrental.lld.billing.BillItemType;
import com.carrental.lld.reservation.VehicleReservation;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Demonstrates late fee on overdue return.
 */
public class ReturnScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Return with late fee ---");
        LocalDateTime start = LocalDateTime.now().minusDays(3).withHour(9).withMinute(0);
        LocalDateTime end = LocalDateTime.now().minusHours(3);

        VehicleReservation reservation = fx.reservationService.reserve(
                fx.alice.getId(), fx.suv.getBarcode(),
                start, end,
                fx.downtown.getBranchId(), fx.airport.getBranchId(),
                List.of(), Collections.emptyList());

        fx.reservationService.pickup(reservation.getReservationNumber());
        double beforeReturn = reservation.getBill().getTotal();

        fx.reservationService.returnVehicle(reservation.getReservationNumber());
        double lateFee = reservation.getBill().getItems().stream()
                .filter(i -> i.getType() == BillItemType.FINE)
                .mapToDouble(i -> i.getAmount()).sum();

        System.out.println("One-way return: downtown → airport");
        System.out.println("Bill before late fee: $" + String.format("%.2f", beforeReturn));
        System.out.println("Late fee applied: $" + String.format("%.2f", lateFee));
        System.out.println("Final total: $" + String.format("%.2f", reservation.getBill().getTotal()));
        System.out.println("Vehicle now at branch: " + fx.suv.getBranchId()
                + " status=" + fx.suv.getStatus());
    }
}
