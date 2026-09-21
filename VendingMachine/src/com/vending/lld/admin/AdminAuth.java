package com.vending.lld.admin;

/**
 * Technician PIN. Production would be a hashed badge / HSM.
 */
public final class AdminAuth {
    private final String pin;

    public AdminAuth(String pin) {
        this.pin = pin;
    }

    public boolean authenticate(String attempt) {
        return pin.equals(attempt);
    }
}
