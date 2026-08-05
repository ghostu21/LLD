package com.amazon.lld.demo;

import com.amazon.lld.account.AccessControl;
import com.amazon.lld.account.AccessDeniedException;

/**
 * Demo: guests browse; only members checkout.
 * <p>
 * Interview angle: centralized {@link com.amazon.lld.account.AccessControl}.
 */
public class AccessScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Access Control ---");
        System.out.println("Guest can browse: " + AccessControl.canBrowse(fx.guest));
        System.out.println("Alice can purchase: " + AccessControl.canPurchase(fx.alice));
        System.out.println("Alice can sell: " + AccessControl.canSell(fx.alice.getAccount()));

        try {
            AccessControl.requirePurchase(null);
        } catch (AccessDeniedException e) {
            System.out.println("Guest checkout blocked: " + e.getMessage());
        }
    }
}
