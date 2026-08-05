package com.amazon.lld.account;

/**
 * Thrown when an actor attempts an operation they are not permitted to perform.
 * <p>
 * Why: guests must not checkout; blocked members must not purchase — callers
 * catch this to return a friendly error instead of silent failure.
 */
public class AccessDeniedException extends RuntimeException {

    /**
     * @param message human-readable denial reason
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}
