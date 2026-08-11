# Distributed Job Scheduler — API Reference

Companions: [`README.md`](./README.md) · [`CLASS_AND_DATA_MODEL.md`](./CLASS_AND_DATA_MODEL.md)

In-process Java API (`JobSchedulerApi`). Production would expose the same contracts over gRPC/HTTP.

---

## 1. `scheduleJob`

**What:** Register a one-off or recurring job.

**Logic:**
1. Validate `jobId` uniqueness, cron / `runAt`
2. Compute first `nextRunAt` (timezone-aware for cron)
3. Persist to `JobStore` with `shardKey` (default = `jobId`)
4. Refresh worker time-wheels for owned shards

**Request**
```json
{
  "jobId": "fleet-hex-stats",
  "tenantId": "uber-city-sf",
  "payload": "recalc-hex-stats",
  "schedule": { "type": "RECURRING", "cronExpr": "*/1 * * * *", "timezone": "UTC" },
  "overlapPolicy": "SKIP",
  "catchUpPolicy": "ONE",
  "retry": { "maxAttempts": 3, "initialBackoffMs": 1000, "multiplier": 2.0 },
  "priority": 10
}
```

One-off variant:
```json
{
  "jobId": "dispatch-42",
  "payload": "dispatch:ride:42",
  "schedule": { "type": "ONE_OFF", "runAt": "2026-08-11T12:00:00Z" }
}
```

**Response**
```json
{
  "jobId": "fleet-hex-stats",
  "status": "ACTIVE",
  "nextRunAt": "2026-08-11T12:01:00Z",
  "shardKey": "fleet-hex-stats"
}
```

---

## 2. `cancelJob`

**What:** Mark schedule `CANCELLED` so scanners skip it.

**Logic:** CAS/force status → `CANCELLED`. In-flight leased executions finish or expire by lease TTL (configurable).

**Request:** `DELETE /jobs/{jobId}` or `cancelJob(jobId)`

**Response:** `{ "cancelled": true }`

---

## 3. `getJobStatus`

**What:** Schedule state + recent execution history.

**Response**
```json
{
  "jobId": "fleet-hex-stats",
  "status": "ACTIVE",
  "nextRunAt": "2026-08-11T12:02:00Z",
  "lastRunAt": "2026-08-11T12:01:00.012Z",
  "lastIdempotencyKey": "fleet-hex-stats@1786429260000",
  "recentExecutions": [
    {
      "executionId": "...",
      "status": "SUCCEEDED",
      "attempt": 1,
      "scheduledFireAt": "2026-08-11T12:01:00Z",
      "driftMs": 12
    }
  ]
}
```

---

## 4. `pauseJob` / `resumeJob` (extra)

**Pause:** `ACTIVE → PAUSED` — due scanner ignores.

**Resume:** Recompute `nextRunAt` from *now* (avoids stampede unless catch-up policy applies on overdue ticks).

---

## 5. Internal: lease + execute

Not a public client API; workers call this path:

1. `DueScanner.tick` → `FireIntent(jobId, scheduledFireAt)`
2. `JobExecutor.execute` with fencing token
3. `ExecutionStore.putIfAbsent(idempotencyKey)` — duplicate → no-op if terminal
4. Run handler; on failure → retry backoff or DLQ

**Idempotency key:** `{jobId}@{scheduledFireAtEpochMs}`

---

## 6. Monitoring dashboard (bonus)

`GET /dashboard` → `MonitoringDashboard.Snapshot`

```json
{
  "activeJobs": 12000,
  "pausedJobs": 40,
  "totalExecutions": 5_600_000,
  "succeeded": 5_590_000,
  "failed": 8_000,
  "deadLettered": 200,
  "missed": 1500,
  "p99DriftMs": 800,
  "maxDriftMs": 12000,
  "recentMisses": [{ "jobId": "...", "driftMs": 5200 }]
}
```

**Missed** = actual start − scheduled fire &gt; miss threshold (default 2–5s).
