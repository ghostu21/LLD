package com.reco.lld.account;

/**
 * Centralized authorization for recommendation and feedback APIs.
 * <p>
 * Why: scattering {@code if (role == ...)} across ranking code is how
 * IDOR bugs appear (caller fetching another user's personalized slate).
 */
public final class AccessControl {

    private AccessControl() {}

    public static boolean isActive(User user) {
        return user != null && user.getStatus() == AccountStatus.ACTIVE;
    }

    /** Guests and members may fetch a public popularity slate. */
    public static boolean canGetPublicRecommendations(User actor) {
        return isActive(actor);
    }

    /** Personalized ranking requires an active member or admin. */
    public static boolean canGetPersonalized(User actor) {
        return isActive(actor)
                && (actor.getRole() == UserRole.MEMBER || actor.getRole() == UserRole.ADMIN);
    }

    /**
     * A caller may only request recs for themselves unless they are an admin.
     * Admins still receive item ids + generic reason codes — not peer PII.
     */
    public static boolean canGetRecommendationsFor(User actor, String targetUserId) {
        if (!isActive(actor) || targetUserId == null) return false;
        if (actor.getUserId().equals(targetUserId)) {
            return canGetPublicRecommendations(actor);
        }
        return actor.getRole() == UserRole.ADMIN;
    }

    public static boolean canRecordInteraction(User actor) {
        return canGetPersonalized(actor);
    }

    public static boolean canModerateCatalog(User actor) {
        return isActive(actor) && actor.getRole() == UserRole.ADMIN;
    }

    public static void requireRecommendationsFor(User actor, String targetUserId) {
        if (!canGetRecommendationsFor(actor, targetUserId)) {
            throw new AccessDeniedException(
                    "Not allowed to fetch recommendations for the requested user");
        }
    }

    public static boolean canManagePreferences(User actor) {
        return canGetPersonalized(actor);
    }

    public static void requireRecordInteraction(User actor) {
        if (!canRecordInteraction(actor)) {
            throw new AccessDeniedException("Only active members may record feedback");
        }
    }

    public static void requireManagePreferences(User actor) {
        if (!canManagePreferences(actor)) {
            throw new AccessDeniedException("Only active members may update selected tags");
        }
    }

    public static void requireModerate(User actor) {
        if (!canModerateCatalog(actor)) {
            throw new AccessDeniedException("Only admins may moderate catalog items");
        }
    }
}
