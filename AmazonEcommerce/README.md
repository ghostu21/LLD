# Amazon Ecommerce LLD

Low-level design of an Amazon-like online marketplace that addresses common interview pitfalls (cart races, sync Observer, hardcoded payments, missing returns, fake shipment tracking, etc.).

## Features Required

- **Product Management (Selling):** Sellers can add products to the catalog.
- **Product Search:** Search by product name or category (indexed, not list-scan).
- **Access Control:** Guests can search/view; only registered members can purchase.
- **Shopping Cart:** Add, remove, and update quantities with optimistic locking.
- **Checkout:** Purchase items currently in the cart.
- **Product Reviews:** Members can rate products and leave written reviews.
- **Shipping Information:** Orders carry a shipping address.
- **Order Cancellation:** Cancel only if the order has not yet shipped.
- **Notifications:** Async notifications on order / shipping status changes.
- **Payment Methods:** Credit card and electronic bank transfer (Strategy + Factory).
- **Shipment Tracking:** Track shipment state via carrier trackers + poller.

## Package structure

```
com.amazon.lld
├── account/     Address, Account, Member, Guest, AccessControl, factories, PasswordUtils
├── catalog/     Product, ProductCatalog, ProductSearchIndex, TrieNode, Review, ReviewService
├── cart/        Item, ShoppingCart (versioned), CartVersionException
├── order/       Order, OrderStatus, OrderLog, CheckoutService, OrderService
├── payment/     Payment, PaymentStrategy, CreditCard/BankTransfer strategies, Factory, Processor
├── shipping/    Shipment, ShipmentTracker, UPS/FedEx trackers, ShipmentPoller
├── returns/     ReturnRequest, Refund, ReturnService, ReturnReason, ReturnStatus
├── events/      AsyncEventBus, OrderEvent, NotificationService
├── command/     Command, AddItemToCartCommand, PlaceOrderCommand, …
├── inventory/   InventoryService
└── demo/        AmazonEcommerceService + *Scenario demos
```

## Run

```bash
cd AmazonEcommerce
javac -d out $(find src -name '*.java')
java -cp out com.amazon.lld.demo.AmazonEcommerceService          # all
java -cp out com.amazon.lld.demo.AmazonEcommerceService list     # names
java -cp out com.amazon.lld.demo.AmazonEcommerceService cart     # one
```

Available scenarios: `access`, `catalog`, `cart`, `checkout`, `payment`, `shipping`, `notify`, `returns`, `cancel`, `review`.

## Problems → Solutions

| # | Common mistake | Fix in this codebase |
|---|----------------|----------------------|
| 1 | Concurrent cart updates with no locking | Optimistic versioning on `ShoppingCart` |
| 2 | Sync Observer / no scalable notifications | Async `AsyncEventBus` + `NotificationService` |
| 3 | Hardcoded payment methods | `PaymentStrategy` + `PaymentStrategyFactory` |
| 4 | No real shipment state machine | `ShipmentTracker` (UPS/FedEx) + `ShipmentPoller` |
| 5 | Missing return / refund workflow | `ReturnRequest` + `Refund` + extended order/payment statuses |

## Core flow

```
Guest/Member ──search──► ProductCatalog (Trie)
Member ──addToCart──► ShoppingCart (version check)
Member ──checkout──► CheckoutService
                         ├── AccessControl (member only)
                         ├── InventoryService.reserve
                         ├── PaymentProcessor (Strategy + Factory)
                         ├── Order (UNSHIPPED)
                         └── AsyncEventBus → NotificationService
Order ──ship──► trackingNumber + ShipmentPoller
Order ──return──► ReturnService → Refund
```

## Patterns used

- **Optimistic locking** for cart concurrency  
- **Strategy + Factory** for payments  
- **Observer (async)** via event bus  
- **Command** for cart/order actions  
- **Factory Method** for Member/Guest accounts  
- **Trie** for catalog search  

## Docs

- `README.md` — this file (structure + run)
- `API_REFERENCE.md` — REST APIs with request/response per requirement
- `CLASS_AND_DATA_MODEL.md` — class relationships + DB tables
- `PROBLEMS_AND_SOLUTIONS.md` — 5 interview pitfalls with examples + interviewer perspective
- `INTERVIEW_PREP_GUIDE.md` — patterns, concepts, trap answers
- `INTERVIEW_QUESTIONS.md` — senior (5+ YOE) interview questions by topic

## Notes

This is an LLD teaching / interview codebase — real card gateways, carrier APIs, and Kafka are stubbed where noted in comments.
