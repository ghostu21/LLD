# Car Rental LLD — Interview Questions (5+ YOE)

Senior questions around this rental LLD. Depth and trade-offs over pattern names.

Companions: [`INTERVIEW_PREP_GUIDE.md`](./INTERVIEW_PREP_GUIDE.md) · [`PROBLEMS_AND_SOLUTIONS.md`](./PROBLEMS_AND_SOLUTIONS.md)

---

## 1. Architecture

1. Domain boundaries: inventory, reservation, billing, payment, logs, notifications — what moves out of process first?
2. Why is embedding logs and notifications inside `Vehicle` / `Reservation` a smell?
3. One-way rentals: how do fleet balancing and pricing change your model?
4. Draw object graph for: 2 branches, 1 SUV, Alice reserves, Bob tries overlapping dates.

## 2. Concurrency & Availability

5. Per-vehicle lock vs global lock vs DB transaction — trade-offs?
6. Define overlap precisely for half-open intervals `[start, end)`.
7. Optimistic version on Vehicle vs pessimistic lock — when?
8. Lock timeout 5s — what does the client do on `ReservationTimeoutException`?
9. Search shows available, then reserve fails — acceptable? How do you reduce it?

## 3. Add-ons & Billing

10. Catalog vs `ReservationAddon` snapshot — why both?
11. Price changes after booking — which number wins on the bill?
12. `perReservation` vs per-day charging — examples?
13. Itemized bill vs single total — what ops/finance needs?
14. Taxes and deposits — where do they fit `BillItemType`?

## 4. Payments

15. Why async payment after (or during) reserve?
16. Design idempotency for payment retries.
17. Capture vs auth-hold for rentals — product implications?
18. Refund path when cancel policy returns partial fee.

## 5. Lifecycle & Policies

19. Cancel after ACTIVE (picked up) — allowed?
20. Walk `StandardCancellationPolicy` tiers; how would VIP policy plug in?
21. Late fee: calendar days vs hours — edge cases at timezone boundaries?
22. Extend rental while ACTIVE — re-check overlap how?

## 6. Logs, Events, Ops

23. What belongs in `VehicleLog` vs reservation status history?
24. Sync vs async notifications for OVERDUE — why async still?
25. Exactly-once reminder emails when scheduler retries?

## 7. Search & Fleet

26. Index vehicles for search at 100k fleet size?
27. Maintenance status — how does it interact with availability search?
28. Barcode scan at pickup — API design?

## 8. Senior system thinking

29. LLD → HLD: services, stores, critical path for Reserve.
30. Flash weekend demand: waitlist vs overbooking policy?
31. Biggest mid-level bug in reservation locking — how catch in review?
32. Metrics: reservation conflict rate, payment success, overdue %, lock wait p99.

---

## Scoring signals

| Weak | Strong |
|------|--------|
| “Synchronize everything” | Per-vehicle lock + overlap predicate |
| `new Equipment()` in reserve | Catalog lookup + snapshot |
| Sync email/pay in reserve | Async bus + gateway retries |
| `returned=true` | Status machine + late fee line item |

## Answer cold

1. Lock **per vehicle**; check **overlaps**.  
2. Add-ons = **catalog + snapshot**.  
3. Bills are **itemized**.  
4. Pay/notify are **async**.  
5. Logs are a **service**.  
6. Cancel uses **policy strategy**.  
7. Late return → **FINE** (+ status COMPLETED).  
8. One-way return updates **branch**.
