# Vending Machine LLD

Low-level design of a vending machine that addresses common interview pitfalls (float payment races, display hardcoded into the machine, no stock/expiry states, no refund commands, no technician mode).

## Features Required

- **Hardware ports:** Coin acceptor, bill acceptor, card reader, dispenser, display.
- **Payments:** Coins, bills, cards, mobile — Strategy; cents not {@code float}.
- **Inventory:** Quantities, low-stock, out-of-stock, expiry, restock.
- **Dispense:** Debit → CAS decrement → hardware dispense → change.
- **UI:** Event-driven display (plus audit + alerts), not a single coupled observer.

## Package structure

```
com.vending.lld
├── money/       Money (integer cents)
├── catalog/     Item, SnackItem, DrinkItem, ItemFactory
├── inventory/   Inventory, InventorySlot, StockState (Available/Low/Out)
├── payment/     PaymentStrategy, Coin/Card/Bill/Mobile
├── command/     CollectPaymentCommand, RefundCommand, TransactionLog
├── hardware/    CoinAcceptor, BillAcceptor, CardReader, Dispenser
├── events/      AsyncEventBus, Display/Audit/Alert listeners
├── machine/     VendingMachine, Idle/Collecting/Maintenance states
├── admin/       AdminAuth
└── demo/        VendingMachineService + *Scenario
```

## Run

```bash
cd VendingMachine
javac -d out $(find src -name '*.java')
java -cp out com.vending.lld.demo.VendingMachineService
java -cp out com.vending.lld.demo.VendingMachineService list
java -cp out com.vending.lld.demo.VendingMachineService purchase
```

Scenarios: `purchase`, `card`, `insufficient`, `stock`, `expiry`, `refund`, `discount`, `concurrency`, `admin`, `events`.

## Problems → Solutions

| # | Common mistake | Fix in this codebase |
|---|----------------|----------------------|
| 1 | `float` balance + unsynchronized `+=` | `AtomicInteger` cents + CAS debit |
| 2 | Display hardcoded on the machine | `AsyncEventBus` (display, audit, alerts) |
| 3 | No OOS / reload / expiry | Slot `StockState` Available / LowStock / OutOfStock |
| 4 | No refund / discount / log | `PaymentCommand` + `TransactionLog` |
| 5 | No technician mode | PIN → `MaintenanceState` restock / price |

## Core flow

```
Idle ──select──► selected code + price on bus
Idle ──coin/bill/card──► Collecting (PaymentStrategy)
Collecting ──complete──► CollectPaymentCommand
                           ├── slot.tryDispense (CAS)
                           ├── Dispenser
                           ├── RefundCommand (change)
                           └── Idle
Collecting ──cancel──► RefundCommand → Idle
Idle ──PIN──► Maintenance (restock / setPrice) ──exit──► Idle
```

## Patterns used

- **Strategy** — payment rails
- **Factory** — snack vs drink
- **State** — machine modes + stock levels
- **Command** — collect / refund with undo
- **Observer / event bus** — display is a subscriber
- **Hardware facade** — acceptors / dispenser

## Docs

- `HLD.md`, `API_REFERENCE.md`, `CLASS_AND_DATA_MODEL.md`
- `PROBLEMS_AND_SOLUTIONS.md`, `INTERVIEW_PREP_GUIDE.md`, `INTERVIEW_QUESTIONS.md`

## Notes

Teaching LLD — motors, bill validators, and card networks are stubbed. Money is **cents**; Java has no `AtomicFloat`.
