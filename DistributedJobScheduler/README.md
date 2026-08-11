# Distributed Job Scheduler LLD

Low-level design of a **distributed job scheduler** (Uber Cadence–style / fleet-dispatch cron): durable schedules, sharded time-wheel due selection, lease-based workers, idempotent execution, and a monitoring dashboard for drift / missed fires.

Inspired by Uber Cadence (distributed cron at hex/city scale), Google SRE cron, and Airbnb Dynein timer patterns.

## Clarifying questions (ask first)

| Question | Assumption if not given |
|----------|-------------------------|
| Job volume / minute? | ~10K/min steady; burst ~100K/sec at cron `:00` boundaries |
| Exactly-once vs at-least-once? | **At-least-once** dispatch + idempotency keys → effectively-once |
| One-off, recurring, or both? | **Both** (`runAt` + cron + timezone) |

## Features Required

### From the prompt
- **Job storage & scheduling:** Durable store with `nextRunAt` index; in-memory **time-wheel** for near-term jobs; **shard key** partitioning
- **Execution & idempotency:** Workers **pull via lease**; idempotency key per fire; **DLQ** after max retries
- **Scalability:** Coordinator → Shard Manager → Worker Pool → Job Store; **consistent hashing**; separate *when* vs *what*
- **Failure handling:** Leader election (simulated etcd/ZK); heartbeat eviction; **hybrid clock** for skew
- **APIs:** `scheduleJob`, `cancelJob`, `getJobStatus`
- **Bonus:** Monitoring dashboard for missed executions & scheduling drift

### Extra (from Cadence / Google cron / industry designs)
- Pause / resume / update schedules
- Timezone-aware cron + DST via `ZoneId`
- **Overlap policy:** ALLOW / SKIP / REPLACE
- **Catch-up policy:** SKIP / ONE / ALL (bounded window)
- Retry with exponential backoff
- **Fencing tokens** so stale leaders cannot claim
- Jitter to smooth thundering herds at cron boundaries
- Execution history for audit / status API

## Package structure

```
com.jobscheduler.lld
├── job/         Job, JobExecution, ScheduleSpec, policies, RetryPolicy
├── store/       JobStore, ExecutionStore, DeadLetterQueue
├── schedule/    CronExpression, TimeWheel, DueScanner
├── coordinate/  Coordinator, LeaderElection, ShardManager, ConsistentHashRing
├── worker/      JobExecutor, WorkerPool
├── api/         JobSchedulerApi
├── monitor/     MonitoringDashboard
├── clock/       HybridClock
└── demo/        JobSchedulerService + *Scenario demos
```

## Run

```bash
cd DistributedJobScheduler
javac -d out $(find src -name '*.java')
java -cp out com.jobscheduler.lld.demo.JobSchedulerService          # all
java -cp out com.jobscheduler.lld.demo.JobSchedulerService list     # names
java -cp out com.jobscheduler.lld.demo.JobSchedulerService lease    # one
```

Available scenarios: `schedule`, `cancel`, `timewheel`, `lease`, `dlq`, `shard`, `catchup`, `monitor`.

## Problems → Solutions

| # | Common mistake | Fix in this codebase |
|---|----------------|----------------------|
| 1 | Scan full DB every second | `TimeWheel` for near-term + sharded `nextRunAt` index |
| 2 | Push-assign jobs to workers | Lease pull; expired lease → reclaim |
| 3 | Exactly-once without idempotency | At-least-once + `jobId@fireEpoch` dedup in `ExecutionStore` |
| 4 | Infinite retries | `RetryPolicy` + `DeadLetterQueue` |
| 5 | Two coordinators double-fire | `LeaderElection` + fencing token |
| 6 | Trust wall clock alone | `HybridClock` + skew tolerance |
| 7 | Missed fires after outage ignored/stampede | `CatchUpPolicy` (SKIP/ONE/ALL) + window |

## Core flow

```
Client ──scheduleJob──► JobSchedulerApi ──persist──► JobStore (nextRunAt, shardKey)
                              │
Coordinator (leader) ──assigns shards──► ShardManager (consistent hash + heartbeats)
                              │
WorkerPool ──per owned shard──► DueScanner (TimeWheel.advance)
                              │
                         FireIntent ──lease──► JobExecutor
                              │
                    ExecutionStore (idempotency) ──handler──► success | retry | DLQ
                              │
                    MonitoringDashboard (drift / missed)
```

## Patterns used

- **Leader election + fencing tokens** for single-writer shard assignment  
- **Consistent hashing** for shard → worker ownership  
- **Time wheel** for O(1) near-term due selection  
- **Lease / visibility timeout** for crash-safe pull execution  
- **Idempotency key** for effectively-once side effects  
- **Strategy-like policies** for overlap, catch-up, retry  
- **Hybrid clock** for skew-tolerant due checks  

## Docs

- `HLD.md` — **high-level design**: final diagram, tech choices vs alternatives, components, interview talking points
- `README.md` — this file
- `API_REFERENCE.md` — APIs (what, logic, request/response)
- `CLASS_AND_DATA_MODEL.md` — class relationships + tables
- `PROBLEMS_AND_SOLUTIONS.md` — pitfalls with interviewer view
- `INTERVIEW_PREP_GUIDE.md` — talk track & scale numbers
- `INTERVIEW_QUESTIONS.md` — senior (5+ YOE) questions

## Notes

Plain Java LLD — Postgres/DynamoDB, etcd/ZooKeeper, and Kafka/SQS are simulated in-memory. Cron parser is a minimal 5-field subset (production: cron-utils / Quartz).
