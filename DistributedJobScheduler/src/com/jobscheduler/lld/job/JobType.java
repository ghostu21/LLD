package com.jobscheduler.lld.job;

/**
 * One-off (runAt) vs recurring (cron) schedules.
 * <p>
 * Why: interviewers expect both; storage and re-arm logic differ.
 */
public enum JobType {
    ONE_OFF,
    RECURRING
}
