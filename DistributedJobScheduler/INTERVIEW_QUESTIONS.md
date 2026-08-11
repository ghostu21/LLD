# Distributed Job Scheduler — Interview Questions (5+ YOE)

Companions: [`INTERVIEW_PREP_GUIDE.md`](./INTERVIEW_PREP_GUIDE.md) · [`PROBLEMS_AND_SOLUTIONS.md`](./PROBLEMS_AND_SOLUTIONS.md)

---

## Design & correctness

1. Why is exactly-once dispatch considered unrealistic here, and how do you still protect a payment / fleet-dispatch side effect?
2. Design the idempotency key for a recurring cron. Why include the *scheduled* fire time, not the actual start time?
3. Compare lease-based pull vs coordinator push for worker assignment under crash and GC pauses.
4. How do fencing tokens interact with shard leases after a false failover (old leader still alive)?
5. Walk through overlap policies (ALLOW / SKIP / REPLACE). Which default would you pick for Uber hex-stat recalculation?

## Scheduling internals

6. Explain a hierarchical time-wheel vs a min-heap priority queue for due selection. Complexity? Memory?
7. How do you page far-horizon jobs into the wheel without missing fires or overloading the DB?
8. How do you handle DST / timezone when computing `next_run_at` for `0 9 * * *` in `America/Los_Angeles`?
9. Design catch-up after a 45-minute outage for a `*/1 * * * *` job with policies SKIP / ONE / ALL.
10. How does jitter reduce thundering herds at minute/hour boundaries?

## Scale & storage

11. Sketch a DynamoDB key design for the due index (Dynein-style). How do you avoid a hot partition at `:00`?
12. How would you implement claiming with Postgres (`FOR UPDATE SKIP LOCKED`) vs conditional updates in DynamoDB?
13. Separate “when” and “what” tiers — what breaks if you don’t?
14. Multi-tenant fairness: one tenant schedules 10M jobs at noon. How do you protect others?

## Failure & ops

15. Worker GC pause exceeds lease TTL mid-handler. What happens? How do you avoid double side effects?
16. Coordinator lost leadership but hasn’t noticed yet. Where must the fencing check live?
17. Define SLIs: scheduling drift p99, miss rate, DLQ rate. What alerts would you page on?
18. Prefer skip vs double-launch (Google SRE cron). When would you invert that preference?

## Bonus / stretch

19. How would you evolve this into Cadence-like workflows (activities, history replay) without rewriting the timer service?
20. Design a backfill API that re-runs a time range without colliding with the live schedule’s idempotency keys.
