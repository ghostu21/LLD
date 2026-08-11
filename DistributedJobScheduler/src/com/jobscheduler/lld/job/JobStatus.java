package com.jobscheduler.lld.job;

/**
 * Lifecycle of a durable schedule (not a single execution).
 */
public enum JobStatus {
    ACTIVE,
    PAUSED,
    CANCELLED,
    COMPLETED
}
