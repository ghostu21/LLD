package com.vending.lld.demo;

/**
 * Cancel issues RefundCommand and returns to IDLE.
 */
public final class RefundScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Cancel / refund ---");
        fx.machine.selectItem("S1");
        fx.machine.insertCoin(100);
        fx.machine.cancelTransaction();
        System.out.println("State: " + fx.machine.stateName());
        System.out.println("Log: " + fx.machine.transactionLog().entries());
    }
}
