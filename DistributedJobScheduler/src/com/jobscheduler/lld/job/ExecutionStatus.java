package com.jobscheduler.lld.job;

/**
 * Lifecycle of one firing attempt / run.
 */
public enum ExecutionStatus {
    LEASED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    DEAD_LETTERED,
    SKIPPED
}
