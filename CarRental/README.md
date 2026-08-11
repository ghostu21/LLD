# Car Rental LLD

Low-level design of a multi-branch car rental system that addresses common interview pitfalls (double-booking, sync payments/notifications, entangled vehicle logs, ad-hoc add-ons, missing itemized billing, etc.).

## Features Required

- **Vehicle inventory:** Cars, trucks, SUVs, vans, motorcycles — unique barcode, parking stall, branch location, vehicle log.
- **Member operations:** Search available vehicles, reserve, cancel, pickup, return (including **one-way** to another branch).
- **Member ↔ vehicle queries:** Who has this vehicle? Which vehicles has this member rented?
- **Late fees:** Auto-calculate when returned after due date.
- **Notifications:** Reservation confirmed, pickup approaching, due approaching, overdue — **async**.
- **Add-ons:** Insurance, equipment (GPS, child seat, ski rack), services (roadside, extra driver, Wi‑Fi) via catalogs + billable snapshots.
- **Multi-branch:** Airports / city branches; pickup ≠ return location supported.

## Package structure

```
com.carrental.lld
├── account/      Member, PasswordUtils
├── branch/       Branch
├── vehicle/      Vehicle, VehicleType, VehicleStatus, VehicleInventory
├── reservation/  VehicleReservation, ReservationService, CancellationPolicy
├── addon/        BillableAddon, Equipment, ServiceAddon, InsuranceProduct, catalogs, ReservationAddon
├── billing/      Bill, BillItem, BillingService
├── payment/      PaymentGateway, PaymentService (async + retry)
├── log/          VehicleLog, VehicleLogService
├── events/       AsyncEventBus, NotificationService
└── demo/         CarRentalService + *Scenario demos
```

## Run

```bash
cd CarRental
javac -d out $(find src -name '*.java')
java -cp out com.carrental.lld.demo.CarRentalService          # all
java -cp out com.carrental.lld.demo.CarRentalService list     # names
java -cp out com.carrental.lld.demo.CarRentalService overlap  # one
```

Available scenarios: `inventory`, `reserve`, `overlap`, `addon`, `payment`, `log`, `notify`, `cancel`, `return`, `member`.

## Problems → Solutions

| # | Common mistake | Fix in this codebase |
|---|----------------|----------------------|
| 1 | Concurrent / overlapping reservations | Per-vehicle `ReentrantLock` + overlap check |
| 2 | Sync payment, no retries | Async `PaymentService` + gateway + exponential backoff |
| 3 | Logs inside `Vehicle` | Dedicated `VehicleLogService` |
| 4 | Sync notifications | `AsyncEventBus` + `NotificationService` |
| 5 | Ad-hoc equipment/services | `BillableAddon` + catalogs + `ReservationAddon` snapshots + itemized `Bill` |

## Core flow

```
Member ──search──► VehicleInventory (type/branch/dates)
Member ──reserve──► ReservationService
                      ├── lock(vehicle)
                      ├── overlap check
                      ├── CONFIRMED + vehicle RESERVED
                      ├── BillingService (base + add-ons)
                      └── AsyncEventBus → NotificationService
pickup → ACTIVE / RENTED + log
return → COMPLETED / AVAILABLE (+ late fee if overdue)
cancel → CancellationPolicy fee + free vehicle
```

## Patterns used

- **Per-resource locking** for reservation concurrency  
- **Strategy** for cancellation policy + payment gateways  
- **Catalog / registry** for reusable add-ons  
- **Snapshot** (`ReservationAddon`) for price-at-booking  
- **Observer (async)** via event bus  
- **Facade / Service** for reservation, billing, logs, payments  

## Docs

- `HLD.md` — **high-level design**: final diagram, tech choices vs alternatives, components, interview talking points
- `README.md` — this file
- `API_REFERENCE.md` — REST APIs (what, logic, request/response)
- `CLASS_AND_DATA_MODEL.md` — class relationships + DB tables
- `PROBLEMS_AND_SOLUTIONS.md` — pitfalls with examples + interviewer view
- `INTERVIEW_PREP_GUIDE.md` — patterns & trap answers
- `INTERVIEW_QUESTIONS.md` — senior (5+ YOE) questions

## Notes

Plain Java LLD — Stripe SDKs, real push providers, and DB transactions are stubbed/simulated where noted.
