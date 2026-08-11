package com.jobscheduler.lld.job;

/**
 * What to do when the previous run is still active at the next fire time.
 * <p>
 * Mirrors Cadence Schedule overlap policies used in production cron systems.
 */
public enum OverlapPolicy {
    /** Allow concurrent overlapping runs. */
    ALLOW,
    /** Skip this fire if previous still running. */
    SKIP,
    /** Cancel/replace the previous run and start a new one. */
    REPLACE
}
