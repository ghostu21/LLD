package com.reco.lld.request;

import com.reco.lld.experiment.ExperimentBucket;

import java.util.Collections;
import java.util.List;

/**
 * Ranked slate returned to the caller — items only, no peer PII.
 */
public final class RecommendationResponse {
    private final String requestId;
    private final List<RecommendedItem> items;
    private final String strategyName;
    private final ExperimentBucket bucket;
    private final boolean cached;

    public RecommendationResponse(String requestId, List<RecommendedItem> items,
                                  String strategyName, ExperimentBucket bucket, boolean cached) {
        this.requestId = requestId;
        this.items = Collections.unmodifiableList(items);
        this.strategyName = strategyName;
        this.bucket = bucket;
        this.cached = cached;
    }

    public String getRequestId() { return requestId; }

    public List<RecommendedItem> getItems() { return items; }

    public String getStrategyName() { return strategyName; }

    public ExperimentBucket getBucket() { return bucket; }

    public boolean isCached() { return cached; }
}
