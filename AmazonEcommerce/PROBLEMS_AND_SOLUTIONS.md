# Amazon Ecommerce LLD — Problems Faced & Solutions

Each item is a common LLD interview pitfall from the design screenshots: **what goes wrong**, a **concrete example**, how **this codebase fixes it**, and what the **interviewer is really probing**.

Companions: [`INTERVIEW_PREP_GUIDE.md`](./INTERVIEW_PREP_GUIDE.md) · [`INTERVIEW_QUESTIONS.md`](./INTERVIEW_QUESTIONS.md)

---

## Quick Map

| # | Problem | Fix |
|---|---------|-----|
| 1 | Concurrent cart updates → inconsistent cart | Optimistic locking via cart `version` |
| 2 | Notifications not async / not scalable | `AsyncEventBus` + listeners |
| 3 | Hardcoded payment methods | Strategy + `PaymentStrategyFactory` |
| 4 | No real-time shipment state tracking | Carrier `ShipmentTracker` + `ShipmentPoller` |
| 5 | Missing return / refund state machine | `ReturnRequest` + `Refund` + extended enums |

---

## 1. Concurrent Cart Updates (No Locking)

### Problem
Multiple tabs/devices mutate the same cart with no conflict detection → lost updates, wrong totals, oversell risk at checkout.

### Example
```
Tab A reads cart version=1, adds Phone
Tab B reads cart version=1, adds Book
Both write without checking → one add can be lost (last-write-wins)

Optimistic fix:
  addItem(item, expectedVersion=1)
  if (cart.version != 1) throw CartVersionException
  else apply change; version → 2
```

### Solution
`ShoppingCart` keeps an `int version`. Every mutate (`addItem` / `removeItem` / `updateQuantity`) requires the client’s `expectedVersion`. Mismatch → `CartVersionException`. Client refreshes and retries.

**Code:** `cart/ShoppingCart.java`, `cart/CartVersionException.java`  
**Demo:** `cart`

### Interviewer perspective
They want you to name **optimistic vs pessimistic** locking and when each fits. Carts are user-scoped with low contention → optimistic versioning is a strong default. Bonus: mention Redis/`VERSION` column in DB for multi-node.

---

## 2. Real-Time Notification System (Sync / Missing Bus)

### Problem
Sync `notifyObservers()` on the request thread does not scale. Order/shipping updates need **event-driven** fan-out (Kafka/RabbitMQ in production).

### Example
```
Bad:  order.ship() → for (observer : list) observer.update(...)  // blocks checkout

Good: order.ship()
        → AsyncEventBus.publish(ORDER_SHIPPED)
        → worker threads → NotificationService (email/push)
Checkout returns immediately; notifications happen async.
```

### Solution
In-process `AsyncEventBus` with `OrderEventListener`. `NotificationService` prints push/email-style messages. Production next step: Kafka / SNS + consumer groups.

**Code:** `events/AsyncEventBus.java`, `events/NotificationService.java`, `events/OrderEvent.java`  
**Demo:** `notify`

### Interviewer perspective
Empty “Observer Pattern” claims fail here. They listen for **async + fan-out + failure isolation** (one bad listener must not kill others). Ask yourself: what happens when 1M users get a flash-sale shipping update?

---

## 3. Flexible Payment Handling (Hardcoded Methods)

### Problem
`if (type == CREDIT_CARD) … else if (BANK) …` inside checkout. Adding UPI/PayPal requires editing the checkout path — Open/Closed violation.

### Example
```
Bad:  checkout() { if ("CARD") chargeCard(); else if ("BANK") wire(); }

Good: PaymentStrategy strategy = PaymentStrategyFactory.get(methodType);
      PaymentResult result = strategy.pay(payment);
```

Adding `UPI` = new strategy class + one factory registration — checkout unchanged.

### Solution
- `Payment` = value object (amount, status, method, txn id) — **not** a Template Method twin of Strategy  
- `PaymentStrategy` + `CreditCardPaymentStrategy` / `BankTransferPaymentStrategy`  
- `PaymentStrategyFactory` + `PaymentProcessor`

**Code:** `payment/*`  
**Demo:** `payment`

### Interviewer perspective
They check whether you **separate creation from behavior** (Factory + Strategy) and whether you accidentally duplicated Template Method *and* Strategy as the same class. Strong answer: Payment is data; Strategy is behavior; Factory selects behavior.

---

## 4. Shipment Tracking Enhancement

### Problem
Order flips to `SHIPPED` with no carrier state machine → customers cannot see IN_TRANSIT / OUT_FOR_DELIVERY / DELIVERED. No polling or webhooks.

### Example
```
Order O1 shipped with UPS tracking 1Z999
ShipmentPoller.pollOnce():
  status = UpsShipmentTracker.getStatus("1Z999")  // IN_TRANSIT
  order.updateShipmentStatus(IN_TRANSIT)
  bus.publish(SHIPMENT_UPDATED)

Later poll → OUT_FOR_DELIVERY → DELIVERED → order COMPLETED
```

Production: carrier webhooks preferred; poller is fallback.

### Solution
`ShipmentTracker` interface → `UpsShipmentTracker` / `FedExShipmentTracker` via factory. `ShipmentPoller.pollOnce()` advances statuses and can publish events.

**Code:** `shipping/ShipmentTracker.java`, `shipping/ShipmentPoller.java`, …  
**Demo:** `shipping`

### Interviewer perspective
They want a **provider abstraction** (Strategy/Adapter) plus an update mechanism (poll vs webhook). Saying “call UPS API” without a poller/webhook story is incomplete.

---

## 5. Missing Return and Refund Handling

### Problem
No return request object, no clear transitions for disputes, no refund payment states → CS and logistics cannot coordinate.

### Example
```
Order DELIVERED
  → ReturnService.requestReturn(order, DEFECTIVE)
  → OrderStatus.RETURN_REQUESTED, ReturnStatus.REQUESTED
  → approve → ITEM_RECEIVED
  → completeRefund → PaymentStatus.REFUNDED, OrderStatus.REFUND_APPLIED
  → bus.publish(REFUND_COMPLETED)
```

### Solution
`ReturnRequest` tracks reason + status + refund amount. `Refund` tracks money movement. `OrderStatus` includes `RETURN_REQUESTED`, `RETURNED`, `REFUND_APPLIED`. `PaymentStatus` includes `REFUND_INITIATED`, `REFUNDED`.

**Code:** `returns/ReturnRequest.java`, `returns/ReturnService.java`, `returns/Refund.java`  
**Demo:** `returns`

### Interviewer perspective
They probe whether you model **returns as a workflow**, not a boolean `returned=true`. Draw the state machine. Mention logistics (receive item) before releasing refund — fraud control.

---

## How to Use This in an Interview

1. Start with access control + cart versioning (high signal in 10 minutes).
2. Checkout path: inventory reserve → payment strategy → order → async notify.
3. Close with shipment poller + return state machine as “production completeness.”

### One-liners interviewers like

1. Cart mutations need a **version** (optimistic lock).  
2. Notifications must be **async** with fan-out.  
3. Payments = **Strategy + Factory**, not if/else.  
4. Shipping needs a **carrier tracker + poll/webhook**.  
5. Returns need **explicit states** for order and payment.  
6. Guests browse; **only members** checkout.  
7. Search must be **indexed** (Trie / ES).  
8. Cancel only while **not yet shipped**.
