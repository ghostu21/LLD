package com.reco.lld.ranking;

/**
 * Item with a numeric score and a generic reason code (no peer identity).
 */
public final class ScoredItem {
    private final String itemId;
    private final double score;
    private final String reasonCode;

    public ScoredItem(String itemId, double score, String reasonCode) {
        this.itemId = itemId;
        this.score = score;
        this.reasonCode = reasonCode;
    }

    public String getItemId() { return itemId; }

    public double getScore() { return score; }

    public String getReasonCode() { return reasonCode; }

    public ScoredItem withScore(double newScore) {
        return new ScoredItem(itemId, newScore, reasonCode);
    }
}
