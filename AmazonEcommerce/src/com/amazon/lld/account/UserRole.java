package com.amazon.lld.account;

/**
 * Authorization role for marketplace actions.
 * <p>
 * Why: guests browse; members purchase; sellers list products; admins override.
 * {@link AccessControl} maps roles to permitted operations.
 */
public enum UserRole {
    /** Unauthenticated browser — search/view only. */
    GUEST,
    /** Registered buyer — cart and checkout. */
    MEMBER,
    /** Registered seller — add/manage catalog products. */
    SELLER,
    /** Platform administrator. */
    ADMIN
}
