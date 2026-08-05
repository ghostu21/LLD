# Amazon Ecommerce LLD — Interview Questions (5+ YOE)

Senior / SDE-2 style questions around this marketplace LLD. Focus on **depth, trade-offs, and failure modes** — not pattern name-dropping.

Companion: [`INTERVIEW_PREP_GUIDE.md`](./INTERVIEW_PREP_GUIDE.md) · [`PROBLEMS_AND_SOLUTIONS.md`](./PROBLEMS_AND_SOLUTIONS.md)

---

## 1. Architecture & Ownership

1. Walk domain boundaries: catalog, cart, checkout, payment, shipping, returns. What stays in-process vs moves out first at scale?
2. Why is a god `OrderManager` doing cart + pay + ship + notify a smell?
3. Draw the object graph for: one member, two devices, both editing the cart.
4. Where do idempotency keys belong on checkout retries?
5. Guest → Member conversion mid-cart: how do you merge carts safely?

---

## 2. Concurrency & Cart

6. Optimistic vs pessimistic locking for carts — when do you pick each?
7. Walk a lost-update scenario without versioning.
8. Two checkout requests for the last item in stock — how does inventory reserve prevent oversell?
9. `CopyOnWriteArrayList` vs synchronized list for cart lines — trade-offs?
10. Cart version in Redis across pods — what happens on clock skew / failed increment?

---

## 3. Payments (Strategy + Factory)

11. Why Strategy over a switch in `CheckoutService`?
12. How do you add UPI without touching checkout?
13. Payment is a value object; Strategy is behavior — defend the split (vs Template Method duplicate).
14. How do you design refunds to be idempotent if the webhook retries?
15. What do you store instead of raw card numbers (PCI)?

---

## 4. Access Control & Auth

16. Enforce “guests cannot buy” in more than one place — where are the gates?
17. Salt + hash on Account — what else for production auth?
18. Seller vs Member roles: how do you prevent a buyer from listing counterfeit goods (moderation hook)?
19. Blocked account mid-checkout — fail closed how?

---

## 5. Catalog & Search

20. Why is linear product scan unacceptable at Amazon scale?
21. Trie vs inverted index vs Elasticsearch for name/category search?
22. How do you keep search consistent when price/stock change rapidly?
23. Category browse + text query together — how do you compose filters?

---

## 6. Orders, Cancel, Shipping

24. Cancel after `SHIPPED` — what should the API return and why?
25. Model shipment statuses; who owns the source of truth — you or the carrier?
26. Poller vs webhook for UPS/FedEx — trade-offs and failure modes?
27. Exactly-once shipment status updates when poller overlaps webhook?
28. Partial shipment (2 of 3 items) — how does your Order model change?

---

## 7. Notifications & Events

29. Sync Observer on placeOrder — what breaks under load?
30. Design backpressure when notification workers lag during Prime Day.
31. Event ordering: SHIPPED then DELIVERED arrive out of order — how do you handle it?
32. At-least-once delivery of ORDER_PLACED — how does NotificationService stay idempotent?

---

## 8. Returns & Refunds

33. Draw the return state machine including rejection and dispute.
34. Why refund only after ITEM_RECEIVED?
35. Partial return (1 of 3 line items) — data model?
36. Chargeback arrives after REFUND_APPLIED — how do finance states extend?

---

## 9. Reviews & Trust

37. Should only purchasers review? How do you prove purchase without leaking order ids?
38. Rating aggregation under concurrent reviews — atomic average vs recompute?
39. Fake review abuse — what rate limits / signals would you add?

---

## 10. Senior Trade-off / System Thinking

40. Convert this LLD to HLD: services, data stores, critical path for “Buy Now.”
41. Flash sale: 100k users hit one SKU — where do you add queues and what breaks first?
42. Saga vs 2PC for reserve-stock → charge-card → create-order?
43. Biggest correctness bug a mid-level engineer would ship here — and how you’d catch it in review.
44. Metrics/SLOs: checkout success rate, payment latency, cart conflict rate, shipment lag.
45. How would you test optimistic cart versioning and inventory reserve under concurrency?

---

## How Interviewers Score 5+ YOE

| Signal | Weak | Strong |
|--------|------|--------|
| Patterns | Names Strategy | Shows Factory + open/closed add of UPI |
| Concurrency | “Use synchronized” | Versioning, inventory CAS, multi-node |
| Payments | if/else card/bank | Strategy + idempotent capture/refund |
| Shipping | status = SHIPPED | Tracker abstraction + poll/webhook |
| Returns | boolean flag | Explicit state machine + refund timing |

---

## 8 Questions You Should Answer Cold

1. Cart mutations need **optimistic versioning**.  
2. Guests browse; **members** checkout.  
3. Payments = **Strategy + Factory**.  
4. Notifications must be **async**.  
5. Shipping = **tracker + poll/webhook**.  
6. Returns are a **state machine**.  
7. Cancel only **before ship**.  
8. Search must be **indexed**; inventory must be **reserved** before charge.
