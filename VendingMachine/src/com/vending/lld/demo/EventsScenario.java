package com.vending.lld.demo;

/**
 * Display, audit, and alerts all subscribe to the same bus.
 */
public final class EventsScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Event bus (display + audit + alerts) ---");
        fx.addLowQty("L2", 100, 1, 2);
        fx.machine.selectItem("L2");
        fx.machine.insertCoin(100);
        fx.machine.completeTransaction();
        fx.settle();
        System.out.println("Audit lines: " + fx.audit.lines().size());
    }
}
