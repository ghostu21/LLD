package com.carrental.lld.demo;

import com.carrental.lld.log.VehicleLogType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Demonstrates vehicle log write and search.
 */
public class LogScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Vehicle log write/search ---");
        fx.logService.addLog(fx.suv.getBarcode(), VehicleLogType.CLEANING,
                "Interior detailed", "STAFF-01");
        fx.logService.addLog(fx.suv.getBarcode(), VehicleLogType.FUELING,
                "Tank filled to full", "STAFF-02");

        Instant from = Instant.now().minus(1, ChronoUnit.HOURS);
        var logs = fx.logService.search(fx.suv.getBarcode(), from, Instant.now(), null);
        System.out.println("Logs for " + fx.suv.getBarcode() + " (last hour): " + logs.size());
        logs.forEach(l -> System.out.println("  " + l));
    }
}
