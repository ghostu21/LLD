package com.amazon.lld.account;

import java.util.UUID;

/**
 * Creates anonymous {@link Guest} sessions.
 * <p>
 * Why: guests need no credentials — a random session id is enough for
 * browse-only analytics and demo scenarios.
 */
public class GuestFactory implements AccountFactory<Guest> {

    /**
     * @return new guest with a random session id
     */
    @Override
    public Guest create() {
        return new Guest(UUID.randomUUID().toString());
    }
}
