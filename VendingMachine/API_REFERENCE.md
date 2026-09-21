# Vending Machine LLD — API Reference

Embedded controller APIs (could be a local REST/gRPC panel). Base: `/v1`

---

## Requirement → API Map

| Requirement | APIs |
|-------------|------|
| Browse / select | `GET /items`, `POST /select` |
| Pay | `POST /pay/coin`, `POST /pay/bill`, `POST /pay/card` |
| Vend / cancel | `POST /complete`, `POST /cancel` |
| Admin | `POST /admin/login`, `POST /admin/restock`, `POST /admin/price` |

### Errors
| HTTP | When |
|------|------|
| 409 | Wrong machine state |
| 402 | Insufficient funds |
| 410 | Out of stock / expired |
| 401 | Bad admin PIN |

---

## `POST /v1/select`

**Request** `{ "code": "S1" }`  
**Working logic** Idle/Collecting: load slot, reject OOS/expired, publish price.

---

## `POST /v1/pay/coin`

**Request** `{ "cents": 25 }`  
**Working logic** Hardware acceptor validates 5/10/25/100; `AtomicInteger.addAndGet`.

---

## `POST /v1/complete`

**Working logic** `CollectPaymentCommand` → `slot.tryDispense` → dispenser → `RefundCommand` change. Debit rolls back if CAS loses the last unit.

---

## `POST /v1/admin/login`

**Request** `{ "pin": "1234" }` → Maintenance state. Restock/price only here.
