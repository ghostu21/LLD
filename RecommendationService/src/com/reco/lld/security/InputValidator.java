package com.reco.lld.security;

import com.reco.lld.request.Placement;
import com.reco.lld.request.RecommendationRequest;

/**
 * Request-size and placement rules (OWASP-style input validation).
 * <p>
 * Why: unbounded {@code limit} is a cheap CPU / memory DoS against scoring.
 * Product-detail without a seed item is a meaningless query that should 400.
 */
public final class InputValidator {
    public static final int MIN_LIMIT = 1;
    public static final int MAX_LIMIT = 50;

    private InputValidator() {}

    public static void validate(RecommendationRequest.Builder b) {
        if (b.getActor() == null) {
            throw new ValidationException("Authenticated actor is required");
        }
        if (b.getPlacement() == null) {
            throw new ValidationException("placement is required");
        }
        if (b.getLimit() < MIN_LIMIT || b.getLimit() > MAX_LIMIT) {
            throw new ValidationException("limit must be between " + MIN_LIMIT + " and " + MAX_LIMIT);
        }
        if (b.getPlacement() == Placement.PRODUCT_DETAIL
                && (b.getSeedItemId() == null || b.getSeedItemId().isBlank())) {
            throw new ValidationException("PRODUCT_DETAIL requires seedItemId");
        }
    }

    public static void requireItemId(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            throw new ValidationException("itemId is required");
        }
    }
}
