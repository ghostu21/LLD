package com.carrental.lld.demo;

import com.carrental.lld.reservation.VehicleReservation;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Demonstrates member ↔ vehicle mapping queries.
 */
public class MemberScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Member ↔ vehicle queries ---");
        LocalDateTime start = LocalDateTime.now().plusDays(40).withHour(14).withMinute(0);
        LocalDateTime end = start.plusDays(1);

        VehicleReservation reservation = fx.reservationService.reserve(
                fx.alice.getId(), fx.truck.getBarcode(),
                start, end,
                fx.airport.getBranchId(), fx.airport.getBranchId(),
                List.of(), Collections.emptyList());

        fx.reservationService.pickup(reservation.getReservationNumber());

        VehicleReservation active = fx.reservationService.getActiveReservationForVehicle(
                fx.truck.getBarcode());
        System.out.println("Active rental for " + fx.truck.getBarcode() + ": "
                + (active != null ? active.getMemberId() : "none"));

        List<VehicleReservation> aliceReservations =
                fx.reservationService.getReservationsByMember(fx.alice.getId());
        System.out.println("Alice reservations: " + aliceReservations.size());
        aliceReservations.forEach(r -> System.out.println("  " + r.getReservationNumber()
                + " vehicle=" + r.getVehicleBarcode() + " status=" + r.getStatus()));
    }
}
