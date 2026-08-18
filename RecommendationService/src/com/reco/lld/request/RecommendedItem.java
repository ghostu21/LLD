package com.reco.lld.request;

/**
 * One row in a recommendation slate.
 * <p>
 * Why: the client needs item id, display title, score, and a <em>generic</em>
 * reason code. Reason must never be "because user X bought this".
 */
public final class RecommendedItem {
    private final String itemId;
    private final String title;
    private final double score;
    private final String reasonCode;

    public RecommendedItem(String itemId, String title, double score, String reasonCode) {
        this.itemId = itemId;
        this.title = title;
        this.score = score;
        this.reasonCode = reasonCode;
    }

    public String getItemId() { return itemId; }

    public String getTitle() { return title; }

    public double getScore() { return score; }

    public String getReasonCode() { return reasonCode; }

    @Override
    public String toString() {
        return title + " [" + reasonCode + " score=" + String.format("%.2f", score) + "]";
    }
}
