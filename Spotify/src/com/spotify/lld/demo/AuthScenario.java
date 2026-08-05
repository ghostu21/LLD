package com.spotify.lld.demo;

import com.spotify.lld.auth.AuthService;
import com.spotify.lld.auth.AuthToken;

public class AuthScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Authentication (salt + hash + tokens) ---");
        AuthService auth = new AuthService();
        auth.register(fx.alice);
        auth.register(fx.bob);

        AuthToken token = auth.login("alice", "secret123");
        System.out.println("Login OK, token: " + token.getToken().substring(0, 8) + "...");
        System.out.println("Token valid: " + auth.validateToken(token.getToken()).isPresent());

        try {
            auth.login("alice", "wrong-password");
            System.out.println("ERROR: bad password should fail");
        } catch (SecurityException e) {
            System.out.println("Bad password rejected: " + e.getMessage());
        }

        auth.logout(token.getToken());
        System.out.println("After logout, token valid: " + auth.validateToken(token.getToken()).isPresent());
    }
}
