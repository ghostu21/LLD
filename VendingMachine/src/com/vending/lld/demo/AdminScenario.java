package com.vending.lld.demo;

import com.vending.lld.money.Money;

import java.time.Instant;

/**
 * PIN → maintenance → restock + price change; customer ops blocked until exit.
 */
public final class AdminScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Admin / maintenance ---");
        fx.machine.enterMaintenance("0000");
        System.out.println("Bad PIN state: " + fx.machine.stateName());
        fx.machine.enterMaintenance("1234");
        System.out.println("After PIN: " + fx.machine.stateName());
        try {
            fx.machine.selectItem("S1");
            System.out.println("ERROR: select should fail in maintenance");
        } catch (IllegalStateException e) {
            System.out.println("Expected: " + e.getMessage());
        }
        fx.machine.restock("S1", 5, Instant.now().plusSeconds(86_400));
        fx.machine.setPrice("S1", Money.ofCents(175));
        System.out.println("S1 qty: " + fx.machine.slot("S1").getQuantity() + " price: "
                + fx.machine.slot("S1").getItem().getPrice());
        fx.machine.exitMaintenance();
        System.out.println("Back to: " + fx.machine.stateName());
    }
}
