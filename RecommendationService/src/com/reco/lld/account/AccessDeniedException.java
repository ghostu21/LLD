package com.reco.lld.account;

/** Thrown when the caller is authenticated but not allowed to perform the action. */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
