package com.vending.lld.demo;

/**
 * Not enough coins — stay collecting, stock unchanged.
 */
public final class InsufficientScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Insufficient funds ---");
        fx.machine.selectItem("S1");
        fx.machine.insertCoin(25);
        fx.machine.completeTransaction();
        System.out.println("S1 remaining: " + fx.machine.slot("S1").getQuantity() + " (expect 10)");
        System.out.println("State: " + fx.machine.stateName() + " (expect COLLECTING)");
    }
}
