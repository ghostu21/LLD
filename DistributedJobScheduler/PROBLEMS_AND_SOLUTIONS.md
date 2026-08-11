# Distributed Job Scheduler — Problems & Solutions

Companions: [`README.md`](./README.md) · [`INTERVIEW_PREP_GUIDE.md`](./INTERVIEW_PREP_GUIDE.md)

---

## 1. Scanning the entire job table every second

**Mistake:** `SELECT * FROM jobs WHERE next_run_at <= now()` on one unpartitioned table.

**Why it fails:** At 20M schedules the index range + lock contention cannot keep p99 lateness under ~1s, especially at hourly cron spikes.

**Fix:** Partition by shard; each worker scans only owned shards. Load near-term jobs into a **time-wheel** (1s slots, ~1h horizon). Far-horizon jobs stay on disk until paged in.

**Code:** `TimeWheel`, `DueScanner`, `JobStore.findDueInShard`

---

## 2. Push-assigning jobs to a specific worker

**Mistake:** Coordinator RPCs `worker.execute(job)` and assumes success.

**Why it fails:** Worker dies mid-RPC → job lost. Network partition → unclear ownership.

**Fix:** Workers **pull** with a **lease / visibility timeout**. Durable execution row stays `LEASED` until ack or expiry; another worker reclaims.

**Code:** `JobExecutor.assignLease`, `reclaimExpiredLeases`

---

## 3. Promising exactly-once execution

**Mistake:** “We’ll use transactions so each job runs exactly once.”

**Why it fails:** Clocks skew, leases expire, leaders failover — duplicates are inevitable at the dispatch layer.

**Fix:** Promise **at-least-once** dispatch. Derive `idempotencyKey = jobId + '@' + scheduledFireAt`. `ExecutionStore.putIfAbsent` short-circuits duplicates. Handlers must be idempotent (critical for real Uber dispatch actions).

**Code:** `Job.idempotencyKey`, `ExecutionStore.putIfAbsent`, `LeaseIdempotencyScenario`

---

## 4. Retrying forever

**Mistake:** Exponential backoff with no ceiling.

**Why it fails:** Poison payloads melt downstream; queue lag grows without bound.

**Fix:** `RetryPolicy.maxAttempts` then **DeadLetterQueue**. Alert on DLQ depth.

**Code:** `DeadLetterQueue`, `DeadLetterScenario`

---

## 5. Two schedulers fire the same cron

**Mistake:** Every node independently scans the same due set.

**Why it fails:** Double dispatch → double charge / double dispatch of a ride.

**Fix:** **Leader election** assigns shards. Only the owner scans a shard. **Fencing token** rejects claims from a stale leader after failover (Google SRE cron lesson: prefer skip over double-launch when forced to choose).

**Code:** `LeaderElection`, `Coordinator`, fencing check in `JobExecutor`

---

## 6. Trusting wall-clock time

**Mistake:** `if (System.currentTimeMillis() >= nextRun)` on every node.

**Why it fails:** NTP slew / VM pause → early or late fires; failover duplicates.

**Fix:** `HybridClock` + skew tolerance window; idempotency absorbs residual skew. Prefer relative timers / lease TTLs over absolute wall time for ownership.

**Code:** `HybridClock.isDue`, `HybridClock.lateness`

---

## 7. Outage catch-up stampede

**Mistake:** After 2h downtime, immediately fire every missed cron tick.

**Why it fails:** Thundering herd collapses workers and downstream.

**Fix:** `CatchUpPolicy`: SKIP / ONE / ALL inside a **bounded catch-up window**. Add **jitter** at cron boundaries.

**Code:** `CatchUpPolicy`, `DueScanner.catchUpFires`, jitter in `DueScanner`

---

## 8. Entangling “when” and “what”

**Mistake:** Same pool both decides due times and runs heavy payloads.

**Why it fails:** Slow handlers delay the scheduling tick → cascading lateness.

**Fix:** DueScanner enqueues fire intents; WorkerPool/executors scale independently (queue in production: Kafka/SQS).

**Code:** `DueScanner.FireIntent` vs `JobExecutor` / `WorkerPool`

---

## Interviewer view (soundbites)

| Topic | One-liner |
|-------|-----------|
| Semantics | At-least-once + idempotency ≈ effectively-once |
| Scale | Shard + time-wheel; burst capacity sized on `:00` herd |
| Safety | Lease + fencing token + DLQ |
| Ops | Dashboard on drift p99 and missed count |
