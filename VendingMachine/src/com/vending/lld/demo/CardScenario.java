package com.vending.lld.demo;

import com.vending.lld.money.Money;

/**
 * Card rail for D1.
 */
public final class CardScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Card payment ---");
        fx.machine.selectItem("D1");
        fx.machine.insertCard(Money.ofCents(250));
        fx.machine.completeTransaction();
        System.out.println("D1 remaining: " + fx.machine.slot("D1").getQuantity());
    }
}
