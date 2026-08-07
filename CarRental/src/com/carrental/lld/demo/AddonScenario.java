package com.carrental.lld.demo;

import com.carrental.lld.addon.ReservationAddon;
import com.carrental.lld.reservation.VehicleReservation;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Demonstrates equipment/service/insurance add-ons and itemized billing.
 */
public class AddonScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Add-ons and itemized bill ---");
        LocalDateTime start = LocalDateTime.now().plusDays(20).withHour(8).withMinute(0);
        LocalDateTime end = start.plusDays(4);

        List<ReservationAddon> addons = List.of(
                new ReservationAddon(fx.equipmentCatalog.findByCode("GPS"), 1),
                new ReservationAddon(fx.serviceCatalog.findByCode("ROADSIDE"), 1),
                new ReservationAddon(fx.insuranceCatalog.findByCode("INS-COL"), 1));

        VehicleReservation reservation = fx.reservationService.reserve(
                fx.alice.getId(), fx.truck.getBarcode(),
                start, end,
                fx.airport.getBranchId(), fx.downtown.getBranchId(),
                addons, List.of("EXTRA-DRIVER-001"));

        System.out.println("Reservation: " + reservation.getReservationNumber()
                + " (one-way: airport → downtown)");
        reservation.getBill().getItems().forEach(System.out::println);
        System.out.println("Total: $" + String.format("%.2f", reservation.getBill().getTotal()));
    }
}
