package com.vending.lld.demo;

/**
 * Expired slot will not vend.
 */
public final class ExpiryScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Expiry ---");
        fx.addExpired("X1", 100);
        System.out.println("X1 state: " + fx.machine.slot("X1").getState().label());
        fx.machine.selectItem("X1");
    }
}
