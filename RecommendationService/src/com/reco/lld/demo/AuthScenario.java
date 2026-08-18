package com.reco.lld.demo;

import com.reco.lld.account.AuthenticationException;
import com.reco.lld.account.Session;

/**
 * Login, bad password, expired-token style failures, guest session.
 */
public class AuthScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("-- auth --");
        Session ok = fx.auth.login("alice", "secret123");
        System.out.println("Alice session issued: " + ok.getToken().substring(0, 8) + "...");
        try {
            fx.auth.login("alice", "wrong-password");
        } catch (AuthenticationException e) {
            System.out.println("Bad password rejected: " + e.getMessage());
        }
        try {
            fx.auth.requireUser("not-a-real-token");
        } catch (AuthenticationException e) {
            System.out.println("Bogus token rejected: " + e.getMessage());
        }
        System.out.println("Guest role: " + fx.guest.getRole());
        System.out.println("Passwords are salted hashes — never stored or returned in recs.");
    }
}
