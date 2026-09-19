package com.gdrive.lld.demo;

/**
 * Register + login; bad password is rejected.
 */
public final class AuthScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Auth ---");
        System.out.println("Login owner: " + fx.users.login("owner", "ownerpass").getUsername());
        try {
            fx.users.login("owner", "wrong");
            System.out.println("ERROR: bad password should fail");
        } catch (SecurityException e) {
            System.out.println("Expected failure: " + e.getMessage());
        }
    }
}
