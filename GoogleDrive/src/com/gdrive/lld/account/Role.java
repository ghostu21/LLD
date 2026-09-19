package com.gdrive.lld.account;

/**
 * Process-wide role: ADMIN bypasses ACL; USER is gated by permissions.
 */
public enum Role {
    ADMIN,
    USER
}
