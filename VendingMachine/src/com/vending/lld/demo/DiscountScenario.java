package com.vending.lld.demo;

/**
 * 50% off S1 ($1.50 → $0.75), pay a dollar, get change.
 */
public final class DiscountScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Discount ---");
        fx.machine.applyDiscountPercent(50);
        fx.machine.selectItem("S1");
        fx.machine.insertCoin(100);
        fx.machine.completeTransaction();
        System.out.println("S1 remaining: " + fx.machine.slot("S1").getQuantity());
    }
}
