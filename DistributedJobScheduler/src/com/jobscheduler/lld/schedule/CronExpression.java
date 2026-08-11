package com.jobscheduler.lld.schedule;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Minimal 5-field cron parser: {@code min hour dom month dow}.
 * Supports {@code *}, numbers, and step forms like every-N minutes. Good enough for LLD demos.
 * <p>
 * Production would use quartz / cron-utils with full DST-aware calendar math.
 */
public final class CronExpression {
    private final String expr;
    private final Field minute;
    private final Field hour;
    private final Field dayOfMonth;
    private final Field month;
    private final Field dayOfWeek;

    public CronExpression(String expr) {
        this.expr = Objects.requireNonNull(expr).trim();
        String[] parts = this.expr.split("\\s+");
        if (parts.length != 5) {
            throw new IllegalArgumentException("cron must have 5 fields: " + expr);
        }
        this.minute = Field.parse(parts[0], 0, 59);
        this.hour = Field.parse(parts[1], 0, 23);
        this.dayOfMonth = Field.parse(parts[2], 1, 31);
        this.month = Field.parse(parts[3], 1, 12);
        this.dayOfWeek = Field.parse(parts[4], 0, 6); // 0=Sunday
    }

    public String getExpr() {
        return expr;
    }

    /**
     * Next fire strictly after {@code after}, in the given timezone (DST-aware via ZoneId).
     * Starts at the next whole minute after {@code after} so results are never &le; {@code after}.
     */
    public Instant nextAfter(Instant after, ZoneId zone) {
        ZonedDateTime cursor = after.atZone(zone)
                .truncatedTo(ChronoUnit.MINUTES)
                .plusMinutes(1);
        ZonedDateTime limit = cursor.plusYears(2);
        while (!cursor.isAfter(limit)) {
            if (matches(cursor)) {
                Instant candidate = cursor.toInstant();
                if (candidate.isAfter(after)) {
                    return candidate;
                }
            }
            cursor = cursor.plusMinutes(1);
        }
        throw new IllegalStateException("no next fire within 2 years for: " + expr);
    }

    private boolean matches(ZonedDateTime zdt) {
        LocalDateTime ldt = zdt.toLocalDateTime();
        int dow = zdt.getDayOfWeek() == DayOfWeek.SUNDAY ? 0 : zdt.getDayOfWeek().getValue();
        return minute.matches(ldt.getMinute())
                && hour.matches(ldt.getHour())
                && dayOfMonth.matches(ldt.getDayOfMonth())
                && month.matches(ldt.getMonthValue())
                && dayOfWeek.matches(dow);
    }

    private static final class Field {
        private final List<Integer> values;

        private Field(List<Integer> values) {
            this.values = values;
        }

        static Field parse(String token, int min, int max) {
            List<Integer> vals = new ArrayList<>();
            if ("*".equals(token)) {
                for (int i = min; i <= max; i++) {
                    vals.add(i);
                }
            } else if (token.startsWith("*/")) {
                int step = Integer.parseInt(token.substring(2));
                if (step <= 0) {
                    throw new IllegalArgumentException("cron step must be > 0: " + token);
                }
                for (int i = min; i <= max; i += step) {
                    vals.add(i);
                }
            } else if (token.contains(",")) {
                for (String p : token.split(",")) {
                    vals.add(Integer.parseInt(p));
                }
            } else if (token.contains("-")) {
                String[] r = token.split("-");
                int a = Integer.parseInt(r[0]);
                int b = Integer.parseInt(r[1]);
                for (int i = a; i <= b; i++) {
                    vals.add(i);
                }
            } else {
                vals.add(Integer.parseInt(token));
            }
            return new Field(vals);
        }

        boolean matches(int v) {
            return values.contains(v);
        }
    }
}
