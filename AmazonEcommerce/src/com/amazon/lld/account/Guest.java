package com.amazon.lld.account;

/**
 * Unauthenticated browser — search and view only.
 * <p>
 * Why: marketplace allows anonymous product discovery; checkout requires
 * registration. {@link AccessControl#canPurchase} rejects guests.
 * <p>
 * Logic: lightweight identity with no cart and no credentials.
 */
public class Guest {
    private final String sessionId;

    /**
     * @param sessionId ephemeral browser session identifier
     */
    public Guest(String sessionId) {
        this.sessionId = sessionId;
    }

    /** @return ephemeral session id */
    public String getSessionId() { return sessionId; }

    /** @return display label for logs */
    public String getUsername() { return "guest-" + sessionId.substring(0, Math.min(8, sessionId.length())); }
}
