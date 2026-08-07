# Car Rental LLD — Problems Faced & Solutions

Each item is a common LLD interview pitfall from the design screenshots: **problem**, **example**, **fix in this codebase**, **interviewer perspective**.

Companions: [`INTERVIEW_PREP_GUIDE.md`](./INTERVIEW_PREP_GUIDE.md) · [`INTERVIEW_QUESTIONS.md`](./INTERVIEW_QUESTIONS.md)

---

## Quick Map

| # | Problem | Fix |
|---|---------|-----|
| 1 | No concurrency / overlap checks on reserve | Per-vehicle lock + atomic availability |
| 2 | Sync payment, no retries | Async gateway + exponential backoff |
| 3 | Vehicle logs tangled in Vehicle | `VehicleLogService` |
| 4 | Sync notifications | Async event bus |
| 5 | Ad-hoc equipment/services | `BillableAddon` + catalogs + snapshots + itemized bill |

---

## 1. Enhanced Vehicle Reservation (Concurrency / Overlap)

### Problem
Reservations created without locking or overlap checks → two members book the same SUV for the same weekend.

### Example
```
T1: Alice reserves VH-SUV-001  Aug 10–12
T2: Bob   reserves VH-SUV-001  Aug 11–13   (overlaps)
Without lock: both may succeed → double-booking
With lock + overlap: Bob gets VehicleNotAvailableException
```

### Solution
`ReservationService` keeps `Map<barcode, ReentrantLock>`. Under `tryLock(5s)`:
1. Load vehicle  
2. `isVehicleAvailable` — no CONFIRMED/ACTIVE reservation overlapping `[start, end)`  
3. Create CONFIRMED reservation; set vehicle RESERVED  
4. Unlock  

**Code:** `reservation/ReservationService.java`  
**Demo:** `overlap`, `reserve`

### Interviewer perspective
They want **resource-level locking** (not a global lock) and a clear overlap predicate. Mention DB alternative: `SELECT … FOR UPDATE` or exclusion constraint on date ranges.

---

## 2. Optimized Payment and Billing (Async + Retry)

### Problem
Synchronous payment blocks the UI/request thread; no gateway abstraction; no retries on transient failures.

### Example
```
Bad:  reserve() { chargeCard(); }  // blocks 3s; network blip fails whole booking

Good: PaymentService.processPaymentAsync(request)
        → gateway.process
        → on retryable failure: backoff 2^n seconds, max 3 tries
        → CompletableFuture<PaymentResult>
```

### Solution
- `PaymentGateway` interface (`CardPaymentGateway`, `BankPaymentGateway`)  
- `PaymentService` async + retry with exponential backoff  
- `BillingService` builds itemized `Bill` separately from charging  

**Code:** `payment/*`, `billing/BillingService.java`  
**Demo:** `payment`, `addon`

### Interviewer perspective
Separate **bill calculation** from **payment capture**. Idempotency keys prevent double charge on retry. Production: Stripe/Adyen webhooks for final status.

---

## 3. Vehicle Log Management

### Problem
Logs stored/managed inside `Vehicle` → hard to search, update, or extend event types; mixes inventory with audit.

### Example
```
Bad:  vehicle.getLogs().add("oil change")

Good: VehicleLogService.logVehicleEvent(barcode, MAINTENANCE, "...", "tech-9")
      VehicleLogService.getVehicleHistory(barcode, from, to)
```

### Solution
`VehicleLog` + `VehicleLogService` (write/search by vehicle, type, date range). Reservation pickup/return also writes log entries.

**Code:** `log/VehicleLogService.java`  
**Demo:** `log`

### Interviewer perspective
Audit/history is a **separate bounded context**. Bonus: event sourcing for status reconstruction (mentioned in pasted notes; LLD keeps append-only logs).

---

## 4. Enhanced Notification System

### Problem
Sync `sendEmail()` inside reserve/cancel delays the critical path; no batch/push story.

### Example
```
reserve under lock
  → publish RESERVATION_CONFIRMED on AsyncEventBus
  → return reservation immediately
worker → NotificationService → email/push/SMS stubs
```

### Solution
`AsyncEventBus` + `RentalEvent` types (CONFIRMED, CANCELLED, PICKUP_REMINDER, DUE_REMINDER, OVERDUE, RETURNED, …) + `NotificationService`.

**Code:** `events/*`  
**Demo:** `notify`

### Interviewer perspective
Same as Spotify/Amazon: **Observer must be async**. Reminders are scheduled publishers in production (cron/queue), not loops on the request thread.

---

## 5. Vehicle Equipment / Service Handling (Modular Add-ons)

### Problem
`new Equipment()` inside reservation; separate lists for equipment/services/insurance; billing can’t itemize cleanly; catalog prices change rewrite history.

### Example
```
Catalog: GPS $8/day, Roadside $20 flat, CDW $18/day
Reservation 4 days:
  ReservationAddon(GPS, qty=1) → 8*4 = 32
  ReservationAddon(Roadside, perReservation=true) → 20
  ReservationAddon(CDW) → 18*4 = 72
Bill items: BASE + EQUIPMENT + SERVICE + INSURANCE
```

### Solution
1. `BillableAddon` + `AddonCategory`  
2. `Equipment` / `ServiceAddon` / `InsuranceProduct` as **catalog definitions**  
3. Catalogs for lookup (never `new` ad-hoc in reserve flow)  
4. `ReservationAddon` **snapshots** name/price/qty  
5. One unified list on `VehicleReservation`  
6. `Bill` + `BillItem` + `BillingService.generateBill`  

**Code:** `addon/*`, `billing/*`  
**Demo:** `addon`

### Interviewer perspective
**Catalog vs snapshot** is the senior signal — price changes tomorrow must not rewrite yesterday’s bill. Unified add-on list beats parallel equipment/services arrays.

---

## How to Use in an Interview

1. Open with multi-branch + vehicle barcode inventory.  
2. Deep-dive reserve locking + overlap (problem #1).  
3. Show add-on catalogs → itemized bill → async pay.  
4. Close with late fee on return + async overdue notifications.

### One-liners

1. **Lock per vehicle**, check overlaps, then confirm.  
2. **Payment is async** with gateway + retries.  
3. **Logs are a service**, not fields on Vehicle.  
4. **Notifications are async events**.  
5. Add-ons = **catalog definitions + reservation snapshots**.  
6. Bills are **itemized**; late fees are FINE line items.  
7. Cancel uses a **CancellationPolicy** strategy.  
8. One-way return updates vehicle **branch**.
