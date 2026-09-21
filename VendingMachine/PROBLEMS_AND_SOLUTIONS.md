# Vending Machine LLD — Problems Faced & Solutions

From the design screenshots.

---

## Quick Map

| # | Problem | Fix |
|---|---------|-----|
| 1 | Payment `float` race | Cents + `AtomicInteger` / CAS |
| 2 | Display coupled to machine | Event bus + N listeners |
| 3 | No OOS / reload / expiry | `StockState` on each slot |
| 4 | No refund / discount / log | `PaymentCommand` + `TransactionLog` |
| 5 | No maintenance | PIN + `MaintenanceState` |

---

## 1. Concurrency in payment

**Problem:** `CoinPayment.balance += amount` on a `float` tears under two coin pulses.

**Solution:** `AtomicInteger` cents; `tryDebit` CAS. Machine lock still serializes a customer session.

**Demo:** `concurrency`

**Interviewer:** Mention there is no `AtomicFloat`; money is discrete.

---

## 2. Tight coupling in display

**Problem:** `DisplayObserver` baked into `VendingMachine`.

**Solution:** `AsyncEventBus`. Display, audit log, and low-stock alerts subscribe independently.

**Demo:** `events`

---

## 3. Inventory change handling

**Problem:** Only `quantity == 0` checks; no reload, low stock, expiry.

**Solution:** Slot states Available / LowStock / OutOfStock; CAS dispense; restock in admin; expiry → OutOfStock.

**Demo:** `stock`, `expiry`, `admin`

---

## 4. Incomplete payment handling

**Problem:** No refund, discount, or tx log.

**Solution:** `CollectPaymentCommand` / `RefundCommand` with `undo`; percent discount on price; append-only `TransactionLog`.

**Demo:** `refund`, `discount`, `purchase`

---

## 5. No maintenance mode

**Problem:** Cannot refill or change price safely.

**Solution:** Authenticated `MaintenanceState`. Customer select/pay throw. Restock + setPrice then exit.

**Demo:** `admin`
