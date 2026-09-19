package com.gdrive.lld.quota;

/**
 * Thrown when a write would exceed the owner's byte cap.
 */
public final class QuotaExceededException extends RuntimeException {
    public QuotaExceededException(String message) {
        super(message);
    }
}
