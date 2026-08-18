package com.reco.lld.account;

/** Thrown when credentials or session tokens are missing or invalid. */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
