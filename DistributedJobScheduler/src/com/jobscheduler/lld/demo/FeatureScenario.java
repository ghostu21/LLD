package com.jobscheduler.lld.demo;

/**
 * Marker for runnable LLD feature demos.
 */
public interface FeatureScenario {
    void run(DemoFixtures fx) throws Exception;
}
