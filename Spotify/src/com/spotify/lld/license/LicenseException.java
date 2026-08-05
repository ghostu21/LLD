package com.spotify.lld.license;

/**
 * Thrown when a play is attempted without a valid geo/time license.
 * <p>
 * Why: fail loudly on the critical path so callers cannot ignore a soft boolean.
 */
public class LicenseException extends RuntimeException {
    public LicenseException(String message) {
        super(message);
    }
}
