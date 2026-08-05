package com.amazon.lld.demo;

/**
 * Contract for one runnable interview/demo scenario.
 * <p>
 * Why: keeps {@link AmazonEcommerceService} open for new demos without editing
 * a giant switch — register a new implementation in the SCENARIOS map.
 */
@FunctionalInterface
public interface FeatureScenario {
    /**
     * Execute the scenario using shared fixtures.
     *
     * @param fx pre-built members, guest, and sample products
     */
    void run(DemoFixtures fx) throws Exception;
}
