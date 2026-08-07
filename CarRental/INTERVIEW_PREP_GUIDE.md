# Car Rental LLD — Interview Prep Guide

Read before the interview: **what to build**, **patterns**, **concepts**, **traps**.

---

## 1. What Are We Building?

A **multi-branch car rental** platform:

| Feature | LLD meaning |
|---------|-------------|
| Inventory | Typed vehicles with barcode + stall + branch |
| Search & reserve | Availability for date range under lock |
| Cancel | Policy-based fee; free vehicle |
| Pickup / return | Status transitions + logs; one-way branch change |
| Late fees | Hours overdue × `lateFeePerHour` |
| Notifications | Async events for confirm / due / overdue |
| Add-ons | Catalog BillableAddons → ReservationAddon snapshots |
| Billing / pay | Itemized Bill + async PaymentGateway |

**One-liner:**  
> “I’m designing in-process LLD for rental: per-vehicle locks against double-booking, catalog add-ons with price snapshots, itemized billing, async payments/notifications, and a dedicated vehicle log service.”

---

## 2. Patterns

| Pattern | Where | Why |
|---------|-------|-----|
| Per-resource lock | `ReservationService` | Serialize bookings per car |
| Strategy | `CancellationPolicy`, `PaymentGateway` | Swap policies/gateways |
| Catalog / Registry | Equipment/Service/Insurance catalogs | Reusable definitions |
| Snapshot | `ReservationAddon` | Freeze price at booking |
| Observer async | `AsyncEventBus` | Non-blocking notifications |
| Facade | Reservation/Billing/Payment/Log services | Hide workflows |

---

## 3. Core Concepts

### Overlap
```
available if no CONFIRMED/ACTIVE reservation with
  start < otherEnd AND end > otherStart
```

### Reserve under lock
```
tryLock(vehicle)
  check available
  save reservation CONFIRMED
  vehicle → RESERVED
unlock
publish RESERVATION_CONFIRMED
```

### Status
```
AVAILABLE → RESERVED → RENTED → AVAILABLE
Reservation: CONFIRMED → ACTIVE → COMPLETED | CANCELLED
```

### Add-on charge
```
perReservation ? price * qty : price * qty * days
```

### Cancel fee (standard)
```
>48h before pickup → 0
>24h → 20% of total
else → 50%
```

---

## 4. Trap → Answer

| Trap | Strong answer |
|------|---------------|
| Global synchronized reserve | Lock **per vehicle** |
| No date overlap check | Interval overlap predicate |
| `new GPS()` in reservation | Lookup catalog; snapshot into ReservationAddon |
| Separate equipment/services lists forever | Unified `List<ReservationAddon>` |
| Bill = one double total | Itemized `BillItem`s |
| Sync email in reserve | Async bus |
| Logs on Vehicle entity | `VehicleLogService` |
| Sync payment | Gateway + async retry |
| Cancel after completed | Guard status |
| Ignore one-way return | Update `vehicle.branchId` on return |

---

## 5. Whiteboard (60s)

1. Search available by type/branch/dates.  
2. Reserve under lock + overlap.  
3. Attach add-ons from catalogs → snapshots.  
4. Generate itemized bill; pay async.  
5. Pickup → ACTIVE; return → late fee if needed; logs + events.

---

## 6. Run demos

```bash
javac -d out $(find src -name '*.java')
java -cp out com.carrental.lld.demo.CarRentalService overlap
java -cp out com.carrental.lld.demo.CarRentalService addon
java -cp out com.carrental.lld.demo.CarRentalService payment
java -cp out com.carrental.lld.demo.CarRentalService return
```

---

## 7. Cheat sheet

1. **Lock per vehicle + overlap check.**  
2. **Catalog definitions, reservation snapshots.**  
3. **Itemized bill; late fee as FINE.**  
4. **Async pay with retries; async notify.**  
5. **Logs in VehicleLogService.**  
6. **CancellationPolicy strategy.**  
7. **One-way return updates branch.**  

*See also: `README.md`, `PROBLEMS_AND_SOLUTIONS.md`, `CLASS_AND_DATA_MODEL.md`, `API_REFERENCE.md`, `INTERVIEW_QUESTIONS.md`.*
