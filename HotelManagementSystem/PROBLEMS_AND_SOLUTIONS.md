# Hotel Management LLD — Problems Faced & Solutions

Mapped from the design screenshots: **problem**, **fix**, **interviewer perspective**.

Companions: [`INTERVIEW_PREP_GUIDE.md`](./INTERVIEW_PREP_GUIDE.md) · [`INTERVIEW_QUESTIONS.md`](./INTERVIEW_QUESTIONS.md)

---

## Quick Map

| # | Problem | Fix |
|---|---------|-----|
| 1 | No room searching & booking logic | Inventory + availability calendar |
| 2 | Inefficient sync Observer notifications | Async event bus + Email/SMS/Push |
| 3 | Missing cancellation & refund | `FullRefundBefore24HoursPolicy` |
| 4 | No housekeeping / room service mgmt | Task workflow + service charges |
| 5 | Concurrency / double-booking | Per-room lock (+ version / Redis) |

---

## 1. Room Searching & Booking (Calendar)

### Problem
Guests need to search available rooms and book them, but status-only design cannot support future stays.

### Solution
Each `Room` maintains `Map<LocalDate, Boolean> availabilityCalendar`.

```java
boolean isAvailable(Room room, LocalDate start, int nights) {
    for (int i = 0; i < nights; i++) {
        if (!Boolean.TRUE.equals(room.getAvailabilityCalendar().get(start.plusDays(i))))
            return false;
    }
    return true;
}
```

**Flow:** Search → Lock Room → Validate Availability → Create Booking → Mark Dates Reserved  

**Interview takeaway:** *Room availability is date-based, not status-based.*

**Demo:** `search`, `book`, `overlap`

---

## 2. Event-Driven Notifications

### Problem
Manual Observer lists are not scalable; sync notify blocks booking.

### Solution
Domain events (`BOOKING_CONFIRMED`, `CHECK_IN_REMINDER`, `CHECK_OUT_REMINDER`) published on `AsyncEventBus`. Subscribers: `EmailService`, `SMSService`, `PushNotificationService`.

**Benefits:** loose coupling, async, extensible.

**Demo:** `notify`, `book`

---

## 3. Cancellation & Refund

### Problem
No refund if cancelled ≥ 24h before check-in.

### Solution
`CancellationPolicy` strategy + `FullRefundBefore24HoursPolicy`.

**Workflow:** Cancel Request → Validate Policy → Initiate Refund → Update Booking + Payment Status  

**Interview takeaway:** *Refunds are business rules, not controller logic.*

**Demo:** `cancel`

---

## 4. Housekeeping & Room Service

### Problem
Housekeeping logs and room services are not managed properly.

### Solution
- `HouseKeepingTask` entity + `HousekeepingWorkflow`
- State: `CHECKOUT → BEING_SERVICED → AVAILABLE`
- `List` of `ServiceCharge` on booking for food / amenities / room service

**Interview takeaway:** *Housekeeping is a workflow, not a method.*

**Demo:** `housekeeping`, `payment`

---

## 5. Concurrency Issues

### Problem
Two users book the same room at the same time → double booking.

### Solution
| Approach | Idea |
|----------|------|
| Optimistic (preferred) | `@Version` / `room.version` — mismatch → retry |
| Pessimistic | `synchronized(room)` / `ReentrantLock` per room |
| Distributed | Redis lock → RoomID → TTL (multi-server safe) |

**Interview takeaway:** *Booking systems fail not when traffic spikes — but when locks are missing.*

**Demo:** `concurrent`, `overlap`
