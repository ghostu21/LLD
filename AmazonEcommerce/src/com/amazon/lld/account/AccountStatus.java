package com.amazon.lld.account;

/**
 * Lifecycle state of a registered account.
 * <p>
 * Why: blocked or closed accounts must not checkout or sell even if credentials
 * are valid — status is checked by {@link AccessControl}.
 */
public enum AccountStatus {
    /** Account is in good standing. */
    ACTIVE,
    /** Account suspended; login may work but purchases are denied. */
    BLOCKED,
    /** Account permanently closed. */
    CLOSED
}
