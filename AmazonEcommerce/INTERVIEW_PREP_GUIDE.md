# Amazon Ecommerce LLD — Interview Prep Guide

Read this before the interview. It covers **what to build**, **patterns to name**, **concepts to defend**, and **trap answers** interviewers expect.

---

## 1. What Are We Building?

An **online marketplace** (Amazon-like) with:

| Feature | What it means in LLD |
|---------|----------------------|
| Catalog & search | Indexed lookup (Trie), not scan-a-list |
| Access control | Guest browse; Member purchase; Seller list |
| Cart | Versioned optimistic updates |
| Checkout | Inventory reserve + payment + order create |
| Payments | Pluggable strategies (card / bank) |
| Shipping | Address on order + carrier tracking |
| Cancel | Allowed only pre-ship |
| Notifications | Async event bus on status changes |
| Reviews | Rating + text on products |
| Returns | ReturnRequest + Refund state machine |

**One-liner you can say:**  
> “I’m designing the in-process LLD of a marketplace: versioned carts, strategy-based payments, async order events, carrier shipment polling, and a return/refund workflow — not a single god `OrderService` class.”

---

## 2. Design Patterns Used

| Pattern | Where | Why say it in interview |
|---------|-------|-------------------------|
| **Optimistic locking** | `ShoppingCart.version` | Multi-device cart without heavy locks |
| **Strategy** | `PaymentStrategy` | Swap card/bank without changing checkout |
| **Factory** | `PaymentStrategyFactory`, account factories | Centralize creation; open for new methods |
| **Observer (async)** | `AsyncEventBus` | Order/shipment fan-out without blocking |
| **Command** | `AddItemToCartCommand`, `PlaceOrderCommand` | Encapsulate cart/checkout actions |
| **Facade / Service** | `CheckoutService`, `ReturnService`, `AccessControl` | Hide multi-step workflows |

**Patterns often claimed but weak if missing implementation:** Template Method *and* Strategy both doing payment. Prefer **Payment = data, Strategy = behavior**.

---

## 3. Core Concepts (Must Be Able to Explain)

### Access control
```
Guest  → search / view
Member → cart / checkout / review / cancel / return
Seller → add products (seller role)
```

### Cart concurrency
```
mutate(expectedVersion):
  if expectedVersion != version → CartVersionException
  else apply; version++
```

### Checkout critical path
```
AccessControl.assertCanPurchase
→ InventoryService.reserve
→ PaymentProcessor.pay (Strategy via Factory)
→ create Order (UNSHIPPED)
→ clear cart
→ publish ORDER_PLACED
```

### Cancel rule
```
canCancel = status ∈ { PENDING, UNSHIPPED }
```

### Shipment
```
Track → Chunks of status:
LABEL_CREATED → PICKED_UP → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
```

### Returns
```
DELIVERED → RETURN_REQUESTED → (approve) → ITEM_RECEIVED → REFUNDED / REFUND_APPLIED
```

### Search at scale
- Linear `for (Product p : list)` is **O(n)**  
- **Trie / inverted index**: prefix/partial ~**O(k)** on query length  

### Auth
- Salt + hash passwords — never plaintext on `Account`

---

## 4. Problem → Trap → Your Answer

| # | Interview trap | Strong answer |
|---|----------------|---------------|
| 1 | Unsynchronized cart ArrayList | Optimistic version **or** per-user lock |
| 2 | Guest can checkout | `AccessControl` / role check before pay |
| 3 | `if (paymentType == …)` in checkout | Strategy + Factory |
| 4 | Sync Observer in placeOrder | Async bus; don’t block purchase path |
| 5 | `order.status = SHIPPED` only | Carrier tracker + poller/webhook |
| 6 | `returned = true` boolean | ReturnRequest + refund payment states |
| 7 | Cancel after shipped | Guard on status |
| 8 | Search by looping catalog | Trie / ES index |
| 9 | Plaintext password | Salt + hash |
| 10 | Oversell on parallel checkout | Inventory reserve (CAS / row lock) |

---

## 5. Package & Class Map

```
com.amazon.lld.account     Account, Member, Guest, AccessControl, PasswordUtils, factories
com.amazon.lld.catalog     Product, ProductCatalog, ProductSearchIndex, Review, ReviewService
com.amazon.lld.cart        Item, ShoppingCart, CartVersionException
com.amazon.lld.order       Order, OrderStatus, CheckoutService, OrderService
com.amazon.lld.payment     Payment, PaymentStrategy*, PaymentStrategyFactory, PaymentProcessor
com.amazon.lld.shipping    Shipment, ShipmentTracker*, ShipmentPoller
com.amazon.lld.returns     ReturnRequest, Refund, ReturnService
com.amazon.lld.events      AsyncEventBus, OrderEvent, NotificationService
com.amazon.lld.command     Command, AddItemToCartCommand, PlaceOrderCommand
com.amazon.lld.inventory   InventoryService
com.amazon.lld.demo        AmazonEcommerceService + feature scenarios
```

**Checkout hierarchy to draw on whiteboard:**
```
Member
  └── ShoppingCart (versioned)
        └── CheckoutService
              ├── InventoryService
              ├── PaymentProcessor → Strategy
              ├── Order
              └── AsyncEventBus → NotificationService
```

---

## 6. Whiteboard Flow (60-Second Version)

1. Seller adds Product → catalog + Trie index + inventory stock.  
2. Guest searches; Member adds to cart with version.  
3. Checkout: access check → reserve stock → pay via Strategy → Order.  
4. Event bus notifies member.  
5. Ship → tracking number → poller updates shipment status → events.  
6. Optional: return request → refund → order REFUND_APPLIED.

---

## 7. Trade-offs & Extensions

| Topic | Current LLD choice | Production next step |
|-------|--------------------|----------------------|
| Cart lock | In-memory version int | DB version column / ETag |
| Events | In-process thread pool | Kafka / SNS |
| Payments | Stub strategies | Stripe/Adyen + idempotency keys |
| Tracking | Stub poller | Carrier webhooks + fallback poll |
| Search | In-memory Trie | Elasticsearch |
| Inventory | ConcurrentHashMap reserve | DB row lock / Redis |

**Solid closer:**  
> “This LLD proves cart conflict detection, payment extensibility, async notifications, and return/refund states in one process. At scale I’d move cart versions, inventory, and the event bus to shared infra, but the **domain boundaries stay the same**.”

---

## 8. How to Run Demos Before the Interview

```bash
javac -d out $(find src -name '*.java')
java -cp out com.amazon.lld.demo.AmazonEcommerceService list
java -cp out com.amazon.lld.demo.AmazonEcommerceService cart
java -cp out com.amazon.lld.demo.AmazonEcommerceService payment
java -cp out com.amazon.lld.demo.AmazonEcommerceService shipping
java -cp out com.amazon.lld.demo.AmazonEcommerceService returns
java -cp out com.amazon.lld.demo.AmazonEcommerceService notify
```

Walk one scenario aloud; tie it back to the problem number in `PROBLEMS_AND_SOLUTIONS.md`.

---

## 9. Cheat Sheet — Sentences to Memorize

1. **Cart uses optimistic versioning.**  
2. **Guests browse; members buy.**  
3. **Payments = Strategy + Factory.**  
4. **Observer must be async.**  
5. **Shipment needs tracker + poll/webhook.**  
6. **Returns are a state machine, not a flag.**  
7. **Cancel only before ship.**  
8. **Search must be indexed.**  
9. **Reserve inventory before charging.**  
10. **Passwords are salted hashes.**

---

*Companion overview: `README.md`.*  
*Problems + examples + interviewer view: `PROBLEMS_AND_SOLUTIONS.md`.*  
*Senior questions: `INTERVIEW_QUESTIONS.md`.*
