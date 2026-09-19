package com.gdrive.lld.demo;

@FunctionalInterface
public interface FeatureScenario {
    void run(DemoFixtures fx) throws Exception;
}
