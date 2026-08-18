package com.reco.lld.security;

/** Thrown when a caller exceeds the per-identity request budget. */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
