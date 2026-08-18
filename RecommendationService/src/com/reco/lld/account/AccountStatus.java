package com.reco.lld.account;

/**
 * Lifecycle of a user account.
 * <p>
 * Why: blocked or pending accounts must not receive personalized ranking
 * (and must not write interaction history that could poison models).
 */
public enum AccountStatus {
    PENDING,
    ACTIVE,
    BLOCKED
}
