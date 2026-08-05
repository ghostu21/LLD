package com.spotify.lld.demo;

import com.spotify.lld.license.License;
import com.spotify.lld.license.LicenseException;
import com.spotify.lld.license.LicenseService;

import java.time.Instant;
import java.util.Set;

public class LicensingScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Licensing (country + time window) ---");
        LicenseService licenses = new LicenseService();
        licenses.registerLicense(new License(
                fx.song1.getId(), Set.of("US", "GB"),
                Instant.parse("2020-01-01T00:00:00Z"),
                Instant.parse("2030-12-31T23:59:59Z")));
        licenses.registerLicense(new License(
                fx.song3.getId(), Set.of("IN"),
                Instant.parse("2020-01-01T00:00:00Z"),
                Instant.parse("2030-12-31T23:59:59Z")));

        System.out.println("Queen in US: " + licenses.canPlay(fx.song1.getId(), "US"));
        System.out.println("Queen in IN: " + licenses.canPlay(fx.song1.getId(), "IN"));
        System.out.println("Jai Ho in IN: " + licenses.canPlay(fx.song3.getId(), "IN"));

        try {
            licenses.assertPlayable(fx.song1.getId(), fx.bob.getCountryCode());
            System.out.println("ERROR: should block Bob (IN)");
        } catch (LicenseException e) {
            System.out.println("Blocked: " + e.getMessage());
        }
    }
}
