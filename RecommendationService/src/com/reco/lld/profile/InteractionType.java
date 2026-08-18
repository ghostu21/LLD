package com.reco.lld.profile;

/**
 * Implicit and explicit feedback used to build a profile.
 * <p>
 * Why: mixing signal strengths in one enum keeps collaborative and
 * content-based rankers from treating a view equal to a purchase.
 */
public enum InteractionType {
    VIEW(1),
    CLICK(2),
    LIKE(3),
    PURCHASE(5),
    DISLIKE(-3),
    HIDE(0);

    private final int weight;

    InteractionType(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isBlock() {
        return this == HIDE || this == DISLIKE;
    }
}
