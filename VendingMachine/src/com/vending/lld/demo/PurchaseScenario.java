package com.vending.lld.demo;

/**
 * Coin purchase of S1 ($1.50) with $2.00, expect change.
 */
public final class PurchaseScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Coin purchase ---");
        fx.machine.selectItem("S1");
        fx.machine.insertCoin(100);
        fx.machine.insertCoin(100);
        fx.machine.completeTransaction();
        System.out.println("S1 remaining: " + fx.machine.slot("S1").getQuantity());
        System.out.println("State: " + fx.machine.stateName());
    }
}
