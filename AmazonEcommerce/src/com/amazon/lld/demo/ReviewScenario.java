package com.amazon.lld.demo;

import com.amazon.lld.catalog.Review;

/**
 * Demo: product reviews with rating and text.
 */
public class ReviewScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Product Reviews ---");
        var reviewService = new com.amazon.lld.catalog.ReviewService();
        reviewService.addReview(new Review(fx.phone.getId(), fx.alice.getMemberId(),
                5, "Great phone, fast delivery!"));
        reviewService.addReview(new Review(fx.phone.getId(), fx.alice.getMemberId(),
                4, "Good value for money."));

        var reviews = reviewService.getReviewsForProduct(fx.phone.getId());
        System.out.println("Reviews for " + fx.phone.getName() + ": " + reviews.size());
        for (Review r : reviews) {
            System.out.println("  " + r.getRating() + "★ — " + r.getText());
        }
    }
}
