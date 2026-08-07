# Car Rental LLD — API Reference

REST-style APIs for requirement fulfillment. Each endpoint includes **What**, **Working logic**, **Request/Response**, and **Useful info**.

Auth: `Authorization: Bearer <token>` unless **public**.  
Base: `/v1`

Companions: [`README.md`](./README.md) · [`CLASS_AND_DATA_MODEL.md`](./CLASS_AND_DATA_MODEL.md)

---

## Requirement → API Map

| Requirement | APIs |
|-------------|------|
| Search vehicles | `GET /vehicles/search` |
| Reserve / cancel | `POST /reservations`, `POST /reservations/{id}/cancel` |
| Pickup / return | `POST .../pickup`, `POST .../return` |
| Member↔vehicle | `GET /members/{id}/reservations`, `GET /vehicles/{barcode}/active-reservation` |
| Add-ons | `POST /reservations/{id}/addons` |
| Bill / pay | `GET .../bill`, `POST .../pay` |
| Logs | `GET /vehicles/{barcode}/logs` |
| Notifications | async + `GET /notifications` |
| Catalogs | `GET /catalog/equipment` (etc.) |

### Errors
| HTTP | When |
|------|------|
| 409 | Overlap / not available |
| 408/503 | Lock timeout |
| 422 | Payment failed |
| 400 | Illegal status transition |

---

## `GET /v1/vehicles/search` (public/member)

**What**  
Search available vehicles by type, branch, and date range.

**Working logic**
1. Parse `type`, `branchId`, `start`, `end`.
2. `ReservationService.searchAvailable` / inventory filter: status AVAILABLE (or not overlapping RESERVED/RENTED windows).
3. Exclude barcodes with overlapping CONFIRMED/ACTIVE reservations.
4. Return list with dailyRate, stall, branch.

**Query:** `type=SUV&branchId=B-AIRPORT&start=2026-08-10T10:00:00Z&end=2026-08-12T10:00:00Z`

**Response `200`**
```json
{
  "results": [
    {
      "barcode": "VH-SUV-001",
      "type": "SUV",
      "make": "Honda",
      "model": "CR-V",
      "branchId": "B-AIRPORT",
      "parkingStall": "A-12",
      "dailyRate": 85.0,
      "status": "AVAILABLE"
    }
  ]
}
```

**Useful info**  
Search is best-effort; reserve may still `409` under race — client retries.

---

## `POST /v1/reservations`

**What**  
Creates a confirmed reservation with optional add-ons. Core **Search and Reserve** API. Prevents double-booking.

**Working logic**
1. Auth member.
2. Acquire per-vehicle lock (`tryLock` 5s) → else lock timeout error.
3. Overlap check for CONFIRMED/ACTIVE.
4. Create `VehicleReservation` CONFIRMED; vehicle → RESERVED.
5. Resolve add-on ids from catalogs → `ReservationAddon` snapshots.
6. `BillingService.generateBill` (base + add-ons).
7. Publish `RESERVATION_CONFIRMED` async; release lock; return reservation.

**Request**
```json
{
  "vehicleBarcode": "VH-SUV-001",
  "start": "2026-08-10T10:00:00Z",
  "end": "2026-08-12T10:00:00Z",
  "pickupBranchId": "B-AIRPORT",
  "returnBranchId": "B-DOWNTOWN",
  "addons": [
    { "addonId": "EQ-GPS", "quantity": 1 },
    { "addonId": "SV-ROADSIDE", "quantity": 1 },
    { "addonId": "IN-CDW", "quantity": 1 }
  ]
}
```

**Response `201`**
```json
{
  "reservationNumber": "RES-100",
  "status": "CONFIRMED",
  "vehicleBarcode": "VH-SUV-001",
  "memberId": "M-001",
  "start": "...",
  "end": "...",
  "pickupBranchId": "B-AIRPORT",
  "returnBranchId": "B-DOWNTOWN",
  "addons": [
    { "name": "GPS Navigation", "category": "EQUIPMENT", "chargePreview": 32.0 }
  ],
  "bill": {
    "billId": "B-9",
    "totalAmount": 464.0,
    "paymentStatus": "UNPAID",
    "items": [
      { "type": "BASE_CHARGE", "description": "Vehicle rental (4 day(s))", "amount": 340.0 },
      { "type": "EQUIPMENT", "description": "GPS Navigation x1", "amount": 32.0 }
    ]
  }
}
```

**Response `409`**
```json
{
  "error": {
    "code": "VEHICLE_NOT_AVAILABLE",
    "message": "Vehicle VH-SUV-001 not available: overlapping reservation exists"
  }
}
```

**Useful info**
- Maps to: `ReservationService.reserveVehicle`
- Interview: lock scope = one vehicle; overlap = interval intersection.
- Idempotency-Key recommended for retries.

---

## `POST /v1/reservations/{reservationNumber}/cancel`

**What**  
Cancels a CONFIRMED (or allowed) reservation; applies **CancellationPolicy** fee; frees vehicle.

**Working logic**
1. Load reservation; reject if CANCELLED/COMPLETED.
2. `cancellationPolicy.calculateFee(reservation)`.
3. Status → CANCELLED; vehicle → AVAILABLE (if was RESERVED).
4. Update bill with fee / refund remainder.
5. Publish `RESERVATION_CANCELLED`.

**Response `200`**
```json
{
  "reservationNumber": "RES-100",
  "status": "CANCELLED",
  "cancellationFee": 0.0
}
```

**Useful info**  
Standard policy: >48h free, >24h 20%, else 50%. Strategy-swappable.

---

## `POST /v1/reservations/{reservationNumber}/pickup`

**What**  
Member picks up car at branch (barcode scan flow).

**Working logic**
1. Status must be CONFIRMED; within pickup window (product rule).
2. → ACTIVE; vehicle → RENTED; record `pickupAt`.
3. `VehicleLogService` PICKUP log.
4. Optional payment capture if not pre-paid.

**Response `200`**
```json
{ "reservationNumber": "RES-100", "status": "ACTIVE", "vehicleStatus": "RENTED" }
```

---

## `POST /v1/reservations/{reservationNumber}/return`

**What**  
Return vehicle (possibly **different branch**); compute late fees.

**Working logic**
1. Status ACTIVE.
2. Set `returnAt`, `returnBranchId`; vehicle branch updated; vehicle → AVAILABLE.
3. If `returnAt > end`: `BillingService.appendLateFee` (hours × lateFeePerHour) as FINE item.
4. Status → COMPLETED; log RETURN; publish RETURNED.
5. Return final bill.

**Request**
```json
{
  "returnBranchId": "B-DOWNTOWN",
  "odometer": 45210,
  "damageNotes": null
}
```

**Response `200`**
```json
{
  "reservationNumber": "RES-100",
  "status": "COMPLETED",
  "lateFee": 45.0,
  "bill": { "totalAmount": 509.0, "paymentStatus": "UNPAID" }
}
```

**Useful info**  
One-way rentals change fleet distribution — ops may rebalance later.

---

## `POST /v1/reservations/{id}/addons`

**What**  
Attach catalog add-ons before pickup (while CONFIRMED).

**Working logic**  
Lookup catalog by id → append `ReservationAddon` snapshot → regenerate bill.

**Request**
```json
{ "addonId": "EQ-GPS", "quantity": 1 }
```

**Useful info**  
Never trust client-sent price — always snapshot from catalog server-side.

---

## `GET /v1/reservations/{id}/bill` · `POST /v1/reservations/{id}/pay`

**What**  
Fetch itemized bill; charge asynchronously via gateway.

**Working logic (pay)**
1. Build/load bill.
2. `PaymentService.processPaymentAsync` → gateway with up to 3 retries + backoff.
3. On success: bill PAID; publish PAYMENT_COMPLETED.
4. Return 202 + payment status polling id (or wait in demo).

**Request (pay)**
```json
{ "method": "CARD", "amount": 464.0 }
```

**Response `202`**
```json
{
  "paymentId": "pay-1",
  "status": "PENDING",
  "pollUrl": "/v1/payments/pay-1"
}
```

**Useful info**  
Maps to async payment problem #2. Card gateway demo fails twice then succeeds.

---

## `GET /v1/members/{memberId}/reservations`

**What**  
All reservations for a member (which vehicles they rented).

**Working logic**  
`ReservationService.getReservationsByMember` → list DTOs.

**Response `200`**
```json
{
  "memberId": "M-001",
  "reservations": [
    { "reservationNumber": "RES-100", "vehicleBarcode": "VH-SUV-001", "status": "COMPLETED" }
  ]
}
```

---

## `GET /v1/vehicles/{barcode}/active-reservation`

**What**  
Which member currently holds this vehicle.

**Working logic**  
Find ACTIVE (or RESERVED) reservation for barcode.

**Response `200`**
```json
{
  "barcode": "VH-SUV-001",
  "reservationNumber": "RES-100",
  "memberId": "M-001",
  "status": "ACTIVE"
}
```

---

## `GET /v1/vehicles/{barcode}/logs`

**What**  
Vehicle history (maintenance, pickup, return, accidents). Fulfills **vehicle log** requirement.

**Working logic**  
`VehicleLogService.getVehicleHistory(barcode, from, to)` optional filters.

**Response `200`**
```json
{
  "barcode": "VH-SUV-001",
  "logs": [
    {
      "logType": "MAINTENANCE",
      "description": "Oil change",
      "performedBy": "tech-9",
      "createdAt": "..."
    }
  ]
}
```

**Useful info**  
Logs are not embedded in Vehicle entity — separate service/table.

---

## `GET /v1/catalog/equipment` · `/services` · `/insurance` (public)

**What**  
Reusable add-on definitions for UI selection.

**Working logic**  
Read from `EquipmentCatalog` / `ServiceCatalog` / `InsuranceCatalog`.

**Response**
```json
{
  "items": [
    { "id": "EQ-GPS", "name": "GPS Navigation", "category": "EQUIPMENT", "basePricePerDay": 8.0 }
  ]
}
```

---

## `GET /v1/notifications`

**What**  
Inbox of rental events (confirm, due, overdue).

**Working logic**  
Async bus already fan-outs; inbox reads stored notifications for member.

**Useful info**  
Pickup/due/overdue reminders are scheduled publishers in production.

---

## End-to-end

```
search → reserve (lock + overlap + add-ons + bill)
→ pay async
→ pickup (scan barcode)
→ return (maybe other branch + late fee)
→ logs + notifications throughout
```

## Interview closer

> “Reserve is a locked critical section with an overlap predicate; add-ons come from catalogs as price snapshots; billing is itemized; payment and notifications leave the request thread.”
