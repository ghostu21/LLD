# Recommendation Service — Problems and solutions

Interview pitfalls this LLD is built to survive.

---

## 1. “Just return global top sellers”

**Symptom:** every user sees the same 10 SKUs.  
**Fix:** `RankingStrategyFactory` selects popularity vs content vs collaborative vs similar-items from **placement**, **cold-start**, and **experiment bucket**.

---

## 2. Collaborative filtering leaks people

**Symptom:** API returns “because Alice also bought this” or a list of neighbor user ids.  
**Fix:** `CollaborativeStrategy` only emits item scores + `COLLABORATIVE`. Profiles contain affinities, not emails.

---

## 3. `GET /recs?userId=` as authentication

**Symptom:** anyone who guesses a UUID reads that shopper’s taste (IDOR).  
**Fix:** `AuthService.requireUser(token)` then `AccessControl.requireRecommendationsFor(actor, targetUserId)`.

---

## 4. Model scores a banned item

**Symptom:** historical purchases keep a taken-down SKU in the slate.  
**Fix:** `EligibilityFilter` after ranking; status is a **hard** constraint.

---

## 5. Hide does nothing

**Symptom:** dislike endpoint writes a row that rankers ignore.  
**Fix:** `ProfileService` puts HIDE/DISLIKE on `blockedItemIds`; `BlockedItemFilter` drops them; cache prefix invalidated.

---

## 6. Personalization throws → blank homepage

**Fix:** `FallbackDecorator` catches empty/exception and serves popularity.

---

## 7. Unbounded `limit=1_000_000`

**Fix:** `InputValidator` clamps 1..50 before scoring.

---

## 8. Sync email on every recommend

**Fix:** `AsyncEventBus` — Observer on a thread pool, same idea as the Amazon/Hotel LLDs in this repo.
