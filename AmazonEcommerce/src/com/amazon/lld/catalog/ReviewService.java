package com.amazon.lld.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages product reviews (add and list by product).
 * <p>
 * Why: separates review persistence from catalog indexing. Demo simplifies
 * purchase verification — any member may review.
 * <p>
 * Logic: reviews stored per productId in CopyOnWriteArrayList for safe reads.
 */
public class ReviewService {
    private final Map<String, List<Review>> reviewsByProduct = new ConcurrentHashMap<>();

    /**
     * Adds a review for a product (demo: any member allowed).
     *
     * @param review new review
     */
    public void addReview(Review review) {
        reviewsByProduct
                .computeIfAbsent(review.getProductId(), k -> new CopyOnWriteArrayList<>())
                .add(review);
    }

    /**
     * @param productId product id
     * @return unmodifiable list of reviews
     */
    public List<Review> getReviewsForProduct(String productId) {
        List<Review> list = reviewsByProduct.getOrDefault(productId, List.of());
        return Collections.unmodifiableList(new ArrayList<>(list));
    }
}
