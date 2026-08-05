package com.amazon.lld.cart;

/**
 * Thrown when a cart mutation uses a stale version (optimistic locking conflict).
 * <p>
 * Why: concurrent add/remove/update on the same cart must not silently lose
 * updates — caller should refresh and retry.
 */
public class CartVersionException extends RuntimeException {

    /**
     * @param expected version the client believed it had
     * @param actual   current server version
     */
    public CartVersionException(int expected, int actual) {
        super("Cart version mismatch: expected " + expected + " but was " + actual);
    }
}
