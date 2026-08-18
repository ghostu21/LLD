# Recommendation Service LLD — API Reference

REST-style contracts above this LLD. **Auth:** `Authorization: Bearer <token>` unless noted.

**Base path:** `/v1`

---

## Requirement → API map

| Requirement | API |
|-------------|-----|
| Register / login | `POST /auth/register`, `POST /auth/login` |
| Guest browse | `POST /auth/guest` then recommend |
| Homepage / email slate | `POST /recommendations` |
| Similar items | `POST /recommendations` with `placement=PRODUCT_DETAIL` |
| Feedback | `POST /interactions` |
| Selected tags (User Service) | `PUT /me/tags` |
| Admin ban | `PATCH /items/{id}/status` |
| Debug another user | admin-only `targetUserId` |

---

## POST `/auth/login`

**What:** Issue a session.  
**Logic:** lookup user → active? → constant-time password verify → TTL token.  
**Never** return `passwordHash` or `salt`.

```json
{ "username": "alice", "password": "secret123" }
```

```json
{ "token": "…", "expiresAt": "…" }
```

401 on unknown user **or** bad password (same message — no user enumeration).

---

## POST `/recommendations`

**What:** Ranked slate for a placement.

```json
{
  "placement": "HOME",
  "targetUserId": null,
  "seedItemId": null,
  "limit": 10,
  "excludeItemIds": []
}
```

`targetUserId` defaults to the session user. Non-admins setting another id → **403**.

```json
{
  "requestId": "…",
  "strategyName": "hybrid+fallback+diversity",
  "bucket": "TREATMENT",
  "cached": false,
  "items": [
    { "itemId": "…", "title": "Wireless Headphones", "score": 8.0, "reasonCode": "HYBRID" }
  ]
}
```

| HTTP | When |
|------|------|
| 400 | limit not in 1–50; PDP missing seed |
| 401 | missing/expired token |
| 403 | IDOR or blocked account |
| 429 | rate limit |

**Useful:** reason codes are generic. Do not add `neighborUserIds`.

---

## PUT `/me/tags`

**What:** Replace the session user's selected tags (User Service). These drive `SelectedTagStrategy` even with no clicks.

```json
{ "tags": ["software", "architecture"] }
```

Tags are lowercased and **must exist on some catalog item**. Guests → 403. Unknown tag → 400. Bumps user generation and busts that user's slate cache.

---

## POST `/interactions`

**What:** Record click / purchase / hide.

```json
{ "itemId": "…", "type": "HIDE" }
```

Guests → 403. Unknown item → 400. Invalidates that user’s slate cache.

---

## PATCH `/items/{id}/status` (admin)

```json
{ "status": "BANNED" }
```

Bans take effect on the **filter chain**, even if co-occurrence still scores the SKU.
