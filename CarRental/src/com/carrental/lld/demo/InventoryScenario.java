package com.carrental.lld.demo;

import com.carrental.lld.vehicle.VehicleType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Demonstrates inventory search by type, branch, and date availability.
 */
public class InventoryScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Inventory search ---");
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusDays(3);

        List<?> cars = fx.reservationService.searchAvailable(
                VehicleType.CAR, fx.downtown.getBranchId(), start, end);
        System.out.println("Available CARs at downtown (" + start.toLocalDate() + "): " + cars.size());
        cars.forEach(v -> System.out.println("  " + v));

        List<?> all = fx.reservationService.searchAvailable(
                null, fx.downtown.getBranchId(), start, end);
        System.out.println("All types at downtown: " + all.size());
    }
}
