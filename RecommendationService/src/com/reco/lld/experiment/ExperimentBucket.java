package com.reco.lld.experiment;

/**
 * A/B assignment for homepage ranking.
 * CONTROL = popularity (safe default); TREATMENT = hybrid personalization.
 */
public enum ExperimentBucket {
    CONTROL,
    TREATMENT
}
