package com.carrental.lld.demo;

import com.carrental.lld.reservation.VehicleNotAvailableException;
import com.carrental.lld.reservation.VehicleReservation;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Demonstrates overlapping reservation rejection.
 */
public class OverlapScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Overlap rejection ---");
        LocalDateTime start = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0);
        LocalDateTime end = start.plusDays(3);

        VehicleReservation first = fx.reservationService.reserve(
                fx.alice.getId(), fx.suv.getBarcode(),
                start, end,
                fx.downtown.getBranchId(), fx.downtown.getBranchId(),
                List.of(), Collections.emptyList());
        System.out.println("First reservation OK: " + first.getReservationNumber());

        LocalDateTime overlapStart = start.plusDays(1);
        LocalDateTime overlapEnd = overlapStart.plusDays(2);
        try {
            fx.reservationService.reserve(
                    fx.bob.getId(), fx.suv.getBarcode(),
                    overlapStart, overlapEnd,
                    fx.downtown.getBranchId(), fx.downtown.getBranchId(),
                    List.of(), Collections.emptyList());
            System.out.println("ERROR: overlap should have failed");
        } catch (VehicleNotAvailableException e) {
            System.out.println("Expected failure: " + e.getMessage());
        }
    }
}
