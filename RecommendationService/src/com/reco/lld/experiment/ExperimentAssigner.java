package com.reco.lld.experiment;

/**
 * Deterministic experiment assignment (hash of user id).
 * <p>
 * Why: sticky buckets so the same user does not flip strategies every
 * request — that would poison both UX and offline eval.
 * <p>
 * Logic: {@code floorMod(userId.hashCode(), 100) < 50} → CONTROL.
 */
public class ExperimentAssigner {

    public ExperimentBucket assign(String userId) {
        if (userId == null) return ExperimentBucket.CONTROL;
        int bucket = Math.floorMod(userId.hashCode(), 100);
        return bucket < 50 ? ExperimentBucket.CONTROL : ExperimentBucket.TREATMENT;
    }
}
