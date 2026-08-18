package com.reco.lld.security;

/** Thrown when request fields fail validation (size, required placement, etc.). */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
