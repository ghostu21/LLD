package com.amazon.lld.account;

/**
 * Factory contract for creating account-like actors.
 * <p>
 * Why: separates construction of members vs guests so demos and tests can
 * swap factories without touching business logic.
 *
 * @param <T> created actor type
 */
public interface AccountFactory<T> {
    /**
     * Builds a new actor instance.
     *
     * @return new guest or member
     * @throws Exception if password hashing fails during member creation
     */
    T create() throws Exception;
}
