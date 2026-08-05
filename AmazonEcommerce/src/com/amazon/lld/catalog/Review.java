package com.amazon.lld.catalog;

import java.time.Instant;
import java.util.UUID;

/**
 * Member-authored product review with rating and text.
 * <p>
 * Why: social proof on listings; immutable id and timestamp for audit.
 */
public class Review {
    private final String reviewId;
    private final String productId;
    private final String memberId;
    private final int rating;
    private final String text;
    private final Instant timestamp;

    /**
     * @param productId reviewed product
     * @param memberId  author member id
     * @param rating    1–5 stars
     * @param text      review body
     */
    public Review(String productId, String memberId, int rating, String text) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be 1-5");
        }
        this.reviewId = UUID.randomUUID().toString();
        this.productId = productId;
        this.memberId = memberId;
        this.rating = rating;
        this.text = text;
        this.timestamp = Instant.now();
    }

    /** @return review id */
    public String getReviewId() { return reviewId; }

    /** @return product id */
    public String getProductId() { return productId; }

    /** @return author member id */
    public String getMemberId() { return memberId; }

    /** @return star rating 1–5 */
    public int getRating() { return rating; }

    /** @return review text */
    public String getText() { return text; }

    /** @return creation time */
    public Instant getTimestamp() { return timestamp; }
}
