package com.vending.lld.demo;

/**
 * Last unit → OUT_OF_STOCK; threshold trip → LOW_STOCK.
 */
public final class StockScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Stock states ---");
        fx.addLowQty("L1", 100, 1, 2);
        System.out.println("L1 initial: " + fx.machine.slot("L1").getState().label());
        fx.machine.selectItem("L1");
        fx.machine.insertCoin(100);
        fx.machine.completeTransaction();
        System.out.println("L1 after sale: " + fx.machine.slot("L1").getState().label());
        fx.machine.selectItem("L1");
    }
}
