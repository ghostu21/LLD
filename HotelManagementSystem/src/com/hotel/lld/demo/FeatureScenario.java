package com.hotel.lld.demo;

/**
 * Contract for one runnable interview/demo scenario.
 */
@FunctionalInterface
public interface FeatureScenario {
    void run(DemoFixtures fx) throws Exception;
}
