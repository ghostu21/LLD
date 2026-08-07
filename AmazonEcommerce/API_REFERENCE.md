# Amazon Ecommerce LLD — API Reference

REST-style APIs that fulfill the marketplace requirements.  
These are the **HTTP contracts** above this LLD (services map 1:1 to handlers).

**Auth:** `Authorization: Bearer <token>` for member/seller actions.  
**Public:** marked explicitly (search/view).  
**Base path:** `/v1`

Companions: [`README.md`](./README.md) · [`CLASS_AND_DATA_MODEL.md`](./CLASS_AND_DATA_MODEL.md) · [`PROBLEMS_AND_SOLUTIONS.md`](./PROBLEMS_AND_SOLUTIONS.md)

---

## How to read each API

| Section | Meaning |
|---------|---------|
| **What** | Purpose / requirement fulfilled |
| **Working logic** | Step-by-step server flow |
| **Request / Response** | Contract |
| **Useful info** | Auth, errors, concurrency, LLD mapping, interview tips |

---

## Requirement → API Map

| # | Requirement | APIs |
|---|-------------|------|
| 1 | Product management | create/update product |
| 2 | Product search | search, get product |
| 3 | Access control | guest vs member gates |
| 4 | Shopping cart | get/add/update/remove + `version` |
| 5 | Checkout | `POST /checkout` |
| 6 | Reviews | create/list reviews |
| 7 | Shipping address | `PUT /me/address` + checkout body |
| 8 | Order cancellation | cancel if not shipped |
| 9 | Notifications | async + inbox |
| 10 | Payment methods | method on checkout (Strategy+Factory) |
| 11 | Shipment tracking | ship + get shipment |
| — | Returns/refunds | return workflow APIs |

---

## Common conventions

### Error body
```json
{
  "error": {
    "code": "CART_VERSION_CONFLICT",
    "message": "Cart version mismatch: expected 1 but was 2",
    "details": { "expected": 1, "actual": 2 }
  }
}
```

| HTTP | When |
|------|------|
| 400 | Validation |
| 401 | Not authenticated |
| 403 | Guest purchase / wrong role |
| 404 | Missing resource |
| 409 | Cart version / illegal cancel |
| 422 | Stock / payment failure |
| 429 | Rate limited |

### Useful globals
- Cart mutations **require** `version` (optimistic locking).
- Checkout should send `Idempotency-Key` to avoid double charge on retry.
- Prices on `order_items` are **snapshots** — later product price changes don’t alter past orders.

---

## 0. Auth (enables access control)

### `POST /v1/auth/register` (public)

**What**  
Registers an account (MEMBER/SELLER). Supports **Access Control** by assigning role.

**Working logic**
1. Validate unique username/email.
2. Salt + hash password (`PasswordUtils`) — never store plaintext.
3. Create `Account` with role + ACTIVE status via `MemberFactory` / seller factory.
4. Return public account fields.

**Request**
```json
{
  "username": "alice",
  "password": "secret123",
  "name": "Alice",
  "email": "alice@example.com",
  "role": "MEMBER"
}
```

**Response `201`**
```json
{
  "accountId": "a-111",
  "username": "alice",
  "role": "MEMBER",
  "status": "ACTIVE"
}
```

**Useful info**
- Maps to: `Account`, `MemberFactory`, `PasswordUtils`
- Guest checkout is forbidden — registration is the path to purchase.

---

### `POST /v1/auth/login` (public)

**What**  
Issues a bearer token for member APIs (cart, checkout, orders).

**Working logic**
1. Verify password hash.
2. Reject BLOCKED/CLOSED accounts.
3. Issue token bound to `accountId` + role claims.
4. Return token + expiry.

**Response `200`**
```json
{
  "token": "tok-abc",
  "accountId": "a-111",
  "role": "MEMBER",
  "expiresAt": "2026-08-06T18:00:00Z"
}
```

**Useful info**  
Role in token is used by `AccessControl` (canPurchase / canSell).

---

### `GET /v1/me`

**What**  
Current account profile + default shipping address.

**Working logic**  
Validate token → load `Account` + address → return safe DTO.

**Useful info**  
Used by UI to know if user is guest/member and to prefill checkout address.

---

## 1. Product Management (Selling)

### `POST /v1/products` (SELLER)

**What**  
Seller lists a new product. Fulfills **Product Management (Selling)**.

**Working logic**
1. `AccessControl` — role must allow sell; else `403`.
2. Create `Product` with sellerId, price, category, initial stock.
3. Index name into Trie (`ProductSearchIndex`); set `InventoryService` stock.
4. Return created product.

**Request**
```json
{
  "name": "Smartphone X",
  "description": "Latest model",
  "price": 699.99,
  "category": "ELECTRONICS",
  "stockCount": 50
}
```

**Response `201`** — product JSON with `productId`, `sellerId`, stock

**Useful info**
- Maps to: `Product`, `ProductCatalog`, `InventoryService`
- Category enum: ELECTRONICS, BOOKS, CLOTHING, HOME, OTHER.

---

### `PATCH /v1/products/{productId}` (seller owner)

**What**  
Updates price and/or stock for an owned product.

**Working logic**
1. Verify caller is seller of product.
2. `updatePrice` / inventory adjust.
3. Reindex if name changes.

**Useful info**  
Stock updates should be atomic (versioned inventory row in DB) to avoid lost updates.

---

## 2. Product Search

### `GET /v1/products/search` (public)

**What**  
Guest/member search by name text and/or category. Fulfills **Product Search**.

**Working logic**
1. No auth required.
2. If `q` present → Trie partial match on name.
3. If `category` present → filter by category.
4. Intersect filters; apply `limit`; return cards (id, name, price, stock).

**Query:** `q=smart&category=ELECTRONICS&limit=20`

**Response `200`**
```json
{
  "query": "smart",
  "category": "ELECTRONICS",
  "results": [
    {
      "productId": "p-1",
      "name": "Smartphone X",
      "price": 699.99,
      "category": "ELECTRONICS",
      "stockCount": 50
    }
  ]
}
```

**Useful info**
- Interview: indexed search, not O(n) list scan.
- Production: Elasticsearch with facets (brand, price range).

---

### `GET /v1/products/{productId}` (public)

**What**  
Product detail page payload including rating summary.

**Working logic**  
Load product + inventory + aggregate reviews → DTO.

**Useful info**  
Still public; purchase requires member session at cart/checkout.

---

## 3. Access Control (cross-cutting)

**What**  
Guests may browse; only members purchase; sellers list products.

**Working logic (gate)**
| API class | Guest | Member | Seller |
|-----------|-------|--------|--------|
| Search/view | ✅ | ✅ | ✅ |
| Cart / checkout / reviews write | ❌ 403 | ✅ | ✅ |
| Create product | ❌ | ❌ | ✅ |

**Example `403`**
```json
{
  "error": {
    "code": "ACCESS_DENIED",
    "message": "Only registered members can purchase."
  }
}
```

**Useful info**
- Maps to: `AccessControl`, `AccessDeniedException`, `Guest` vs `Member`
- Enforce on **server** — never trust client UI hiding buttons.

---

## 4. Shopping Cart (optimistic locking)

### `GET /v1/cart`

**What**  
Returns the member’s cart and current **version** (required for safe edits).

**Working logic**
1. Auth member.
2. Load `ShoppingCart`; compute line totals.
3. Return `version` so client can pass it on next mutate.

**Response `200`**
```json
{
  "cartId": "c-1",
  "memberId": "a-111",
  "version": 3,
  "items": [
    {
      "productId": "p-1",
      "name": "Smartphone X",
      "quantity": 1,
      "unitPrice": 699.99,
      "lineTotal": 699.99
    }
  ],
  "total": 699.99
}
```

**Useful info**  
Always show version in GET — multi-tab UIs need it.

---

### `POST /v1/cart/items`

**What**  
Adds (or merges qty of) a product line. Fulfills **Shopping Cart Management** with concurrency safety.

**Working logic**
1. Auth member (`AccessControl.canPurchase`).
2. Validate product exists and qty &gt; 0.
3. `cart.addItem(item, expectedVersion)`:
   - if `expectedVersion != cart.version` → throw `CartVersionException` → **409**
   - else merge/add line, `version++`
4. Return new cart snapshot + new version.

**Request**
```json
{
  "productId": "p-1",
  "quantity": 1,
  "version": 3
}
```

**Response `200`** — cart with `version: 4`  
**Response `409`** — `CART_VERSION_CONFLICT` (client must GET cart and retry)

**Useful info**
- Maps to: `ShoppingCart`, `Item`, `CartVersionException`
- Interview: optimistic locking for multi-device carts; DB form is  
  `UPDATE carts SET version=version+1 WHERE id=? AND version=?`
- Soft stock check here is optional; hard reserve happens at checkout.

---

### `PATCH /v1/cart/items/{productId}` · `DELETE /v1/cart/items/{productId}`

**What**  
Change quantity or remove a line — same version protocol.

**Working logic**  
Same optimistic check → mutate → bump version. Qty 0 on PATCH may equal DELETE.

**Useful info**  
Missing `version` → `400`. Stale `version` → `409` (not `400`).

---

## 5. Checkout

### `POST /v1/checkout`

**What**  
Purchases all cart items: pay, create order, clear cart. Fulfills **Checkout Process** + **Payment Methods** + **Shipping Information**.

**Working logic**
1. Auth member; reject guest.
2. Validate cart non-empty; validate/resolve shipping `Address`.
3. For each line: `InventoryService.reserve(productId, qty)` — fail → `422 INSUFFICIENT_STOCK` (release prior reserves).
4. `PaymentStrategyFactory.get(paymentMethod)` → `strategy.pay(Payment)`  
   - CREDIT_CARD → `CreditCardPaymentStrategy`  
   - BANK_TRANSFER → `BankTransferPaymentStrategy`  
   Fail → release inventory → `422 PAYMENT_FAILED`.
5. Snapshot items + prices into `Order` (status `UNSHIPPED`).
6. Clear cart / bump version.
7. `AsyncEventBus.publish(ORDER_PLACED)` → email/push async.
8. Return order DTO (never block on notification).

**Request**
```json
{
  "paymentMethod": "CREDIT_CARD",
  "shippingAddress": {
    "line1": "1 Main St",
    "city": "Seattle",
    "state": "WA",
    "postalCode": "98101",
    "country": "US"
  }
}
```

**Response `201`**
```json
{
  "orderId": "o-100",
  "status": "UNSHIPPED",
  "items": [
    { "productId": "p-1", "quantity": 1, "unitPrice": 699.99 }
  ],
  "shippingAddress": { "line1": "1 Main St", "city": "Seattle", "country": "US" },
  "payment": {
    "amount": 699.99,
    "methodType": "CREDIT_CARD",
    "status": "COMPLETED",
    "transactionId": "CC-E018CECE"
  },
  "createdAt": "2026-08-06T12:00:00Z"
}
```

**Useful info**
- Maps to: `CheckoutService`, `PaymentProcessor`, `InventoryService`, `AsyncEventBus`
- Use `Idempotency-Key` header so double-submit doesn’t double-charge.
- Interview: payment = Strategy+Factory; don’t hardcode if/else in checkout.
- Order lines are snapshots — cart is not referenced after success.

---

## 6. Product Reviews

### `POST /v1/products/{productId}/reviews`

**What**  
Member rates a product and writes a review. Fulfills **Product Reviews**.

**Working logic**
1. Auth member.
2. Validate rating 1–5; product exists.
3. (Production) verify purchase history — LLD may allow any member.
4. Persist `Review`; return created resource.

**Request**
```json
{
  "rating": 5,
  "text": "Great phone, fast delivery!"
}
```

**Response `201`** — review with `reviewId`, timestamps

**Useful info**
- Maps to: `ReviewService`, `Review`
- Abuse: rate-limit reviews; one review per member per product (upsert).

---

### `GET /v1/products/{productId}/reviews` (public)

**What**  
Lists reviews + average rating for PDP.

**Working logic**  
Load reviews for product; compute average; paginate in production.

---

## 7. Shipping Address

### `PUT /v1/me/address`

**What**  
Saves default shipping address for faster checkout. Fulfills **Shipping Information**.

**Working logic**
1. Auth member.
2. Validate address fields/country.
3. Store on `Account` / `addresses` table.
4. Checkout may still override with body address.

**Request**
```json
{
  "line1": "1 Main St",
  "city": "Seattle",
  "state": "WA",
  "postalCode": "98101",
  "country": "US"
}
```

**Useful info**  
Maps to: `Address`. Order stores address snapshot at checkout time.

---

## 8. Order Cancellation

### `GET /v1/orders/{orderId}`

**What**  
Order detail for buyer (status, payment, tracking).

**Working logic**  
Auth; ensure caller owns order (or admin); return aggregate DTO.

---

### `POST /v1/orders/{orderId}/cancel`

**What**  
Cancels an order **only before shipment**. Fulfills **Order Cancellation**.

**Working logic**
1. Load order; verify owner.
2. If status ∉ {PENDING, UNSHIPPED} → `409 CANCEL_NOT_ALLOWED`.
3. Set status `CANCELED`; release reserved inventory; refund payment if captured.
4. Publish `ORDER_CANCELED` async notification.
5. Return updated order.

**Response `200`**
```json
{ "orderId": "o-100", "status": "CANCELED" }
```

**Response `409`**
```json
{
  "error": {
    "code": "CANCEL_NOT_ALLOWED",
    "message": "Cannot cancel order in status: SHIPPED"
  }
}
```

**Useful info**
- Maps to: `Order.cancel`, `OrderService`
- After SHIPPED, path is **return**, not cancel.

---

## 9. Notifications

### `GET /v1/notifications`

**What**  
In-app inbox of order/shipping/return events. Fulfills **Notifications**.

**Working logic**
1. Domain actions publish `OrderEvent` on `AsyncEventBus`.
2. `NotificationService` handles async (email/push/in-app).
3. Inbox API reads stored notifications for user (newest first).

**Response `200`**
```json
{
  "items": [
    {
      "type": "ORDER_PLACED",
      "orderId": "o-100",
      "message": "Order o-100 confirmed: 699.99 charged via CREDIT_CARD",
      "createdAt": "2026-08-06T12:00:01Z"
    }
  ]
}
```

**Useful info**
- Interview: sync Observer on checkout blocks purchase path — bus must be async.
- Production: Kafka + push (FCM/APNs); at-least-once → idempotent notification keys.

---

## 10. Payment Methods

Payment is selected **inside checkout** (see §5). Supporting APIs:

### `GET /v1/payments/methods` (public/member)

**What**  
Lists supported rails for UI radio buttons.

**Working logic**  
Return enum values backed by `PaymentStrategyFactory` registrations.

**Response**
```json
{
  "methods": [
    { "type": "CREDIT_CARD", "displayName": "Credit Card" },
    { "type": "BANK_TRANSFER", "displayName": "Bank Transfer" }
  ]
}
```

**Useful info**
- Adding UPI = new Strategy class + factory entry — checkout API unchanged.
- Never return raw card numbers; use tokens/PSP refs (PCI).

---

### `GET /v1/orders/{orderId}/payment`

**What**  
Payment status for an order (COMPLETED / REFUNDED / …).

**Working logic**  
Load `Payment` linked to order; mask sensitive fields.

---

## 11. Shipment Tracking

### `POST /v1/orders/{orderId}/ship` (warehouse/admin)

**What**  
Marks order shipped and attaches carrier tracking. Starts tracking lifecycle.

**Working logic**
1. Order must be UNSHIPPED.
2. Create `Shipment(carrier, trackingNumber, LABEL_CREATED)`.
3. `Order.markShipped(trackingNumber)`; status → SHIPPED.
4. Publish `ORDER_SHIPPED` notification.
5. `ShipmentPoller` later calls `UpsShipmentTracker` / `FedExShipmentTracker.getStatus`.

**Request**
```json
{
  "carrier": "UPS",
  "trackingNumber": "UPS1Z999AA10123456"
}
```

**Response `200`** — order + shipment with `LABEL_CREATED`

**Useful info**
- Maps to: `Shipment`, `ShipmentTrackerFactory`, `ShipmentPoller`
- Production prefers **webhooks**; poller is fallback.
- Status path: LABEL_CREATED → PICKED_UP → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED.

---

### `GET /v1/orders/{orderId}/shipment`

**What**  
Buyer tracking page. Fulfills **Shipment Tracking**.

**Working logic**
1. Auth owner.
2. Return current carrier status + optional history timeline.
3. Optionally live-fetch tracker (rate-limit) or serve last polled status.

**Response `200`**
```json
{
  "orderId": "o-100",
  "carrier": "UPS",
  "trackingNumber": "UPS1Z999AA10123456",
  "status": "IN_TRANSIT",
  "history": [
    { "status": "LABEL_CREATED", "at": "..." },
    { "status": "PICKED_UP", "at": "..." },
    { "status": "IN_TRANSIT", "at": "..." }
  ]
}
```

---

## Returns & Refunds

### `POST /v1/orders/{orderId}/returns`

**What**  
Starts return after delivery. Solves **Missing Return/Refund Handling**.

**Working logic**
1. Order typically DELIVERED/COMPLETED (product rule).
2. Create `ReturnRequest(reason, REQUESTED, refundAmount)`.
3. Order → `RETURN_REQUESTED`; publish event.
4. CS approves → logistics receives item → refund.

**Request**
```json
{
  "reason": "DEFECTIVE",
  "notes": "Screen cracked on arrival"
}
```

**Response `201`**
```json
{
  "returnId": "ret-1",
  "orderId": "o-100",
  "reason": "DEFECTIVE",
  "status": "REQUESTED",
  "refundAmount": 699.99
}
```

**Useful info**  
Don’t refund on request alone — wait for ITEM_RECEIVED (fraud control).

---

### `POST /v1/returns/{returnId}/approve` · `POST /v1/returns/{returnId}/refund`

**What**  
CS approval and money movement.

**Working logic (refund)**
1. Validate return approved / item received.
2. Create `Refund`; set payment `REFUND_INITIATED` → `REFUNDED`.
3. Order → `REFUND_APPLIED`; return → `REFUNDED`.
4. Publish `REFUND_COMPLETED`.

**Response `200`**
```json
{
  "returnId": "ret-1",
  "status": "REFUNDED",
  "refund": { "refundId": "rf-1", "amount": 699.99, "status": "REFUNDED" },
  "orderStatus": "REFUND_APPLIED"
}
```

**Useful info**
- Maps to: `ReturnService`, `ReturnRequest`, `Refund`
- Interview: returns are a **state machine**, not `returned=true`.

---

## End-to-end “Buy Now” sequence

```
register/login (MEMBER)
→ search products (public)
→ add to cart with version
→ checkout (address + CREDIT_CARD|BANK_TRANSFER)
    → reserve inventory
    → pay via Strategy/Factory
    → create order snapshot
    → async ORDER_PLACED notification
→ warehouse ships → tracking updates via poller/webhook
→ (optional) return → refund
```

## Cart conflict recovery (client)

```
POST /cart/items (version=3) → 409
→ GET /cart (version=4, see other tab’s changes)
→ merge UI → POST /cart/items (version=4) → 200 version=5
```

## Interview closer

> “APIs stay thin: cart carries optimistic `version`, checkout orchestrates reserve→pay→order→event, payments plug in via Strategy/Factory, shipping statuses come from carrier trackers, and returns/refunds use explicit states.”
