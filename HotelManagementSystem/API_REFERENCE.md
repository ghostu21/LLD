# Hotel Management — API Reference

In-memory service APIs used by demos (map 1:1 to REST in production).

---

## BookingService

| Method | Description |
|--------|-------------|
| `search(style, start, nights)` | Rooms free for the date window |
| `book(guestId, roomNumber, checkIn, nights)` | Lock → validate → confirm → mark reserved |
| `cancel(reservationNumber, cancelAt)` | Policy refund + free calendar |
| `checkIn(reservationNumber)` | CONFIRMED → CHECKED_IN, room OCCUPIED |
| `checkOut(reservationNumber)` | CHECKED_IN → CHECKED_OUT, assign HK |
| `addServiceCharge(reservationNumber, charge)` | Append food/amenity to bill |
| `bookingsForRoom(roomNumber)` | History for a room |
| `bookingsForGuest(guestId)` | History for a guest |
| `publishReminders(today)` | Emits check-in / check-out reminder events |

## HousekeepingWorkflow

| Method | Description |
|--------|-------------|
| `assignAfterCheckout(roomNumber, staffId)` | Create PENDING task, room BEING_SERVICED |
| `start(taskId)` | PENDING → IN_PROGRESS |
| `complete(taskId)` | COMPLETED + room AVAILABLE |

## PaymentService

| Method | Description |
|--------|-------------|
| `registerGateway(method, gateway)` | CREDIT_CARD / CHECK / CASH |
| `processPaymentAsync(request)` | Async charge with retry |

## CancellationPolicy

| Method | Description |
|--------|-------------|
| `calculateRefund(booking, cancelAt)` | Returns `Refund.full` / `Refund.none` |
