package com.reco.lld.request;

import com.reco.lld.account.User;
import com.reco.lld.security.InputValidator;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Immutable recommend API input (Builder pattern).
 * <p>
 * Why: a constructor with 8 args is error-prone; the builder documents
 * required vs optional fields and runs validation once at {@link Builder#build()}.
 */
public final class RecommendationRequest {
    private final User actor;
    private final String targetUserId;
    private final Placement placement;
    private final String seedItemId;
    private final int limit;
    private final Set<String> extraExclusions;

    private RecommendationRequest(Builder b) {
        this.actor = b.actor;
        this.targetUserId = b.targetUserId != null ? b.targetUserId : b.actor.getUserId();
        this.placement = b.placement;
        this.seedItemId = b.seedItemId;
        this.limit = b.limit;
        this.extraExclusions = Collections.unmodifiableSet(new LinkedHashSet<>(b.extraExclusions));
    }

    public User getActor() { return actor; }

    public String getTargetUserId() { return targetUserId; }

    public Placement getPlacement() { return placement; }

    public String getSeedItemId() { return seedItemId; }

    public int getLimit() { return limit; }

    public Set<String> getExtraExclusions() { return extraExclusions; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private User actor;
        private String targetUserId;
        private Placement placement = Placement.HOME;
        private String seedItemId;
        private int limit = 10;
        private Set<String> extraExclusions = new LinkedHashSet<>();

        public Builder actor(User actor) {
            this.actor = actor;
            return this;
        }

        public Builder targetUserId(String targetUserId) {
            this.targetUserId = targetUserId;
            return this;
        }

        public Builder placement(Placement placement) {
            this.placement = placement;
            return this;
        }

        public Builder seedItemId(String seedItemId) {
            this.seedItemId = seedItemId;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder exclude(String itemId) {
            if (itemId != null) extraExclusions.add(itemId);
            return this;
        }

        public RecommendationRequest build() {
            InputValidator.validate(this);
            return new RecommendationRequest(this);
        }

        public User getActor() { return actor; }

        public Placement getPlacement() { return placement; }

        public String getSeedItemId() { return seedItemId; }

        public int getLimit() { return limit; }
    }
}
