package com.jobscheduler.lld.job;

/**
 * How to handle missed fires after an outage (Cadence-style backfill).
 */
public enum CatchUpPolicy {
    /** Drop all missed fires; resume from now. */
    SKIP,
    /** Dispatch at most one catch-up fire, then resume. */
    ONE,
    /** Dispatch all missed fires within the catch-up window. */
    ALL
}
