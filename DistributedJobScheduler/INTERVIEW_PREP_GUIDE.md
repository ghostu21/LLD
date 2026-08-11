# Distributed Job Scheduler — Interview Prep Guide

Companions: [`PROBLEMS_AND_SOLUTIONS.md`](./PROBLEMS_AND_SOLUTIONS.md) · [`INTERVIEW_QUESTIONS.md`](./INTERVIEW_QUESTIONS.md)

---

## 1. Open with clarifying questions (90 seconds)

1. Volume: jobs registered? fires/sec average vs `:00` burst?
2. Semantics: at-least-once OK if handlers are idempotent?
3. Job types: one-off timers, cron, or both? Timezones?
4. SLA: p99 lateness budget (often ~1s)?
5. Multi-tenant? Priority? Overlap when previous run still active?

State assumptions if the interviewer shrugs (see README table).

---

## 2. Scale sketch (say the math)

| Estimate | Example |
|----------|---------|
| Registered schedules | ~20M |
| Avg fire rate | 20M / 3600 ≈ **5.5K/s** |
| Hourly cron burst | 10M into ~30s ≈ **300K+/s** |
| Per-scheduler capacity | ~1K dispatch/s (Dynein-order) → hundreds of instances at peak |
| Hot store | ~30GB schedules; history separate, time-partitioned |

Smoothing: **jitter**, stagger cron expressions, separate queue for execution.

---

## 3. Talk track (8–10 min design)

1. **API + durable JobStore** indexed by `next_run_at`, partitioned by shard  
2. **Coordinator** via etcd/ZK lease; assigns shards; fencing token  
3. **Due path:** time-wheel near-term; DB page-in for horizon  
4. **Dispatch queue** decouples when/what  
5. **Workers** lease → execute → ack; idempotency key; DLQ  
6. **Policies:** overlap, catch-up, retry  
7. **Observability:** drift histogram, missed count, DLQ depth  

Draw boxes before deep-diving any one.

---

## 4. Patterns checklist

| Pattern | Where |
|---------|-------|
| Leader election | `LeaderElection` / Coordinator |
| Consistent hashing | `ConsistentHashRing` / ShardManager |
| Time wheel | `TimeWheel` |
| Lease / visibility timeout | `JobExecutor` |
| Idempotency key | `Job.idempotencyKey` |
| Fencing token | Coordinator → Executor |
| Dead letter | `DeadLetterQueue` |
| Hybrid clock | `HybridClock` |

---

## 5. Trap answers

**Q: How do you get exactly-once?**  
A: You don’t at the network layer. At-least-once dispatch + idempotent sink (and unique fire key) gives effectively-once.

**Q: Postgres or DynamoDB?**  
A: Either works with a time index. DynamoDB pattern (random pk + time sk) spreads hot partitions at `:00`. Postgres needs careful partitioning + `SKIP LOCKED` for claims.

**Q: What if the leader is partitioned with the store?**  
A: Lease expires; new leader takes over with a higher fencing token; old leader’s claims fail CAS.

**Q: Skip vs double-run?**  
A: Google SRE cron prefers **skip** over double when forced — undoing a double newsletter/charge is worse. Product still chooses per-job.

---

## 6. Uber-flavored color

Mention Cadence **Schedules**: server-side cron objects with pause, backfill, overlap policies, and per-entity periodic workflows (e.g. hex stats once/minute across cities). This design is the “distributed cron” slice of that problem, not the full workflow engine.
