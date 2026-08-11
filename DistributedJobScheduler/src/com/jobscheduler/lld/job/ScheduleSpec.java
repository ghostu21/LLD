package com.jobscheduler.lld.job;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

/**
 * Either a one-off runAt or a recurring cron expression (+ timezone).
 */
public final class ScheduleSpec {
    private final JobType type;
    private final Instant runAt;
    private final String cronExpr;
    private final ZoneId timezone;

    private ScheduleSpec(JobType type, Instant runAt, String cronExpr, ZoneId timezone) {
        this.type = Objects.requireNonNull(type);
        this.runAt = runAt;
        this.cronExpr = cronExpr;
        this.timezone = timezone != null ? timezone : ZoneId.of("UTC");
    }

    public static ScheduleSpec oneOff(Instant runAt) {
        Objects.requireNonNull(runAt, "runAt");
        return new ScheduleSpec(JobType.ONE_OFF, runAt, null, ZoneId.of("UTC"));
    }

    public static ScheduleSpec recurring(String cronExpr, ZoneId timezone) {
        Objects.requireNonNull(cronExpr, "cronExpr");
        return new ScheduleSpec(JobType.RECURRING, null, cronExpr, timezone);
    }

    public JobType getType() {
        return type;
    }

    public Optional<Instant> getRunAt() {
        return Optional.ofNullable(runAt);
    }

    public Optional<String> getCronExpr() {
        return Optional.ofNullable(cronExpr);
    }

    public ZoneId getTimezone() {
        return timezone;
    }
}
