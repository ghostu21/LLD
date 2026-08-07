package com.carrental.lld.demo;

/**
 * Contract for one runnable interview/demo scenario.
 * <p>
 * Why: keeps {@link CarRentalService} open for new demos without editing a
 * giant switch — register a new implementation in the SCENARIOS map.
 */
@FunctionalInterface
public interface FeatureScenario {
    /**
     * Execute the scenario using shared fixtures.
     *
     * @param fx pre-built branches, members, vehicles, and wired services
     */
    void run(DemoFixtures fx) throws Exception;
}
