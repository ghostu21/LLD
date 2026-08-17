# Hotel Management System LLD

Low-level design of a hotel booking platform that addresses common interview pitfalls (date-based availability, double-booking, sync notifications, missing refund policy, housekeeping as a one-liner, etc.).

## Features Required

- **Room types:** STANDARD, DELUXE, FAMILY_SUITE, BUSINESS_SUITE.
- **Search & booking:** Date-based availability calendar; search inventory; book any free room.
- **Information retrieval:** Who booked a room? Which rooms did a guest book?
- **Cancellations & refunds:** Full refund if cancelled ≥ 24h before check-in.
- **Notifications:** Booking confirmed, check-in / check-out reminders — **async** via event bus.
- **Housekeeping:** Task workflow `CHECKOUT → BEING_SERVICED → AVAILABLE`.
- **Room service & amenities:** Food / amenity charges on the stay bill.
- **Payments:** Credit card, check, or cash.

## Package structure

```
com.hotel.lld
├── account/      Guest, AccountStatus, AccountType
├── hotel/        Hotel
├── room/         Room, RoomStyle, RoomStatus, RoomInventory, RoomAvailability
├── booking/      RoomBooking, BookingService, CancellationPolicy, Refund
├── service/      HouseKeepingTask, HousekeepingWorkflow, Amenity, ServiceCharge
├── billing/      Bill, BillItem, BillingService
├── payment/      PaymentGateway*, PaymentService (async + retry)
├── events/       AsyncEventBus, Email/SMS/Push services
└── demo/         HotelManagementService + *Scenario demos
```

## Run

```bash
cd HotelManagementSystem
javac -d out $(find src -name '*.java')
java -cp out com.hotel.lld.demo.HotelManagementService          # all
java -cp out com.hotel.lld.demo.HotelManagementService list     # names
java -cp out com.hotel.lld.demo.HotelManagementService overlap  # one
```

Available scenarios: `search`, `book`, `overlap`, `cancel`, `payment`, `notify`, `housekeeping`, `guest`, `concurrent`.

## Problems → Solutions

| # | Common mistake | Fix in this codebase |
|---|----------------|----------------------|
| 1 | Status-only availability / no calendar | `Room.availabilityCalendar` + date-range `isAvailable` |
| 2 | Sync Observer list for notifications | `AsyncEventBus` + Email / SMS / Push subscribers |
| 3 | No cancel / refund rules | `FullRefundBefore24HoursPolicy` + refund on bill |
| 4 | Housekeeping as a method | `HousekeepingWorkflow` + `HouseKeepingTask` |
| 5 | Concurrent double-booking | Per-room `ReentrantLock` (+ room `version` for optimistic) |

## Core flow

```
Guest ──search──► RoomInventory (style + calendar dates)
Guest ──book────► BookingService
                    ├── lock(room)
                    ├── calendar availability
                    ├── CONFIRMED + mark dates reserved
                    ├── BillingService (room + charges)
                    └── AsyncEventBus → Email / SMS / Push
cancel → CancellationPolicy refund + free dates
checkout → BEING_SERVICED → HousekeepingWorkflow → AVAILABLE
```

## Patterns used

- **Per-resource locking** for booking concurrency  
- **Strategy** for cancellation / refund policy + payment gateways  
- **Template-style workflow** for housekeeping tasks  
- **Observer (async)** via event bus  
- **Facade / Service** for booking, billing, payments  

## Docs

- `HLD.md` — high-level architecture & interview talking points
- `README.md` — this file
- `CLASS_AND_DATA_MODEL.md` — classes → relationships → tables
- `PROBLEMS_AND_SOLUTIONS.md` — screenshot pitfalls mapped to code
- `API_REFERENCE.md` — service APIs
- `INTERVIEW_QUESTIONS.md` / `INTERVIEW_PREP_GUIDE.md`
