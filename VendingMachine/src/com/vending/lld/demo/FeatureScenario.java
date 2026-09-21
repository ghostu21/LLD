package com.vending.lld.demo;

@FunctionalInterface
public interface FeatureScenario {
    void run(DemoFixtures fx) throws Exception;
}
