# Recommendation Service — Thought process, patterns, and edge cases

This is the “how to think” companion to the code.  
Use with [`README.md`](./README.md) (what to run) and [`HLD.md`](./HLD.md) (whiteboard architecture).

---

## 1. Thought process (how to start the design)

A recommendation service is easy to over-design (two-tower models, ANN indexes) and easy to under-design (one global top-N list). The useful LLD sits in the middle: **a request pipeline with pluggable scoring, hard filters, and a privacy boundary**.

### 1.1 Clarify before drawing boxes

Ask these first; they change the class diagram:

| Question | What it decides |
|----------|-----------------|
| What are we recommending? | Items vs users vs queries — here: **catalog products** |
| Where does the slate show? | HOME ≠ PDP similar-items ≠ EMAIL (conservative) |
| Who is the caller? | Guest vs member vs admin debug |
| What signals exist? | Explicit **User Service tags**, clicks, purchases, hide |
| What must never appear? | Banned, OOS, hidden, already bought (on HOME) |
| What is a failure? | Empty homepage is worse than a slightly generic one |

If the interviewer only says “design Netflix recommendations,” pin **placement + cold start + privacy** before naming ALS or Transformers.

### 1.2 Split two different problems

1. **Read path (recommend)** — must be fast, safe, and fail open.  
2. **Write path (tags, clicks, hide, ban)** — must be authorized, validated, and must **invalidate** anything derived (profile cache, slate cache).

Mixing them in one controller method is how IDOR and stale hides appear.

### 1.3 Order of work in the pipeline (intentional)

Security and policy run **before** clever ranking:

```
authenticate → authorize (no IDOR) → rate-limit → validate
    → cache (generation-aware)
    → build profile (tags + history, no PII)
    → score (Strategy / Factory / Hybrid)
    → hard filters (ban / hide / purchased / seed)
    → diversity
    → truncate to limit
    → async “slate generated” event
```

Scoring is allowed to be wrong. **Filters are not.** A banned SKU with a high collaborative score is still a bug.

### 1.4 The privacy boundary

Collaborative filtering *uses* other people’s purchases. The HTTP response must not.

| Allowed in the slate | Never in the slate |
|----------------------|--------------------|
| `itemId`, title, score, generic reason (`TAG`, `HYBRID`) | Neighbor user ids, emails, “because Alice bought this” |
| Own selected tags (in logs for the owner) | Another user’s tag set |

`UserProfile` is the boundary object: affinities, selected tags, blocked ids — not `User.email`.

### 1.5 Explicit tags vs inferred tags

Two different signals:

| Signal | Source | Strength | Works with 0 clicks? |
|--------|--------|----------|----------------------|
| Selected tags | User Service onboarding | High (stated intent) | **Yes** |
| Tag affinity | Click/purchase on tagged items | Medium (noisy) | No |

If you dump selected tags into the same map as click weights, you cannot explain or A/B them separately. That is why `SelectedTagStrategy` is its own Strategy, then **Composite**-blended.

---

## 2. Why these design patterns (and why not others)

Patterns here are not decoration. Each one removes a class of interview failure.

### Strategy — `RankingStrategy`

**Problem:** HOME, PDP, EMAIL, cold start, and CONTROL/TREATMENT need different math. A `switch (placement)` in the facade grows forever and is untestable.

**Why Strategy:** each algorithm (`Popularity`, `ContentBased`, `Collaborative`, `SimilarItems`, `SelectedTag`) is a replaceable object with `rank(RankingContext)`.

**Why not one class with flags:** you cannot unit-test “tags only” without dragging popularity code along.

### Factory Method — `RankingStrategyFactory`

**Problem:** the facade should not know “if tags and no history, 55% tags + 45% popularity.”

**Why Factory:** one place that maps *(placement, experiment bucket, profile)* → strategy graph. Adding a new placement is one method, not six controllers.

### Composite — `HybridRankingStrategy`

**Problem:** no single signal is enough. Popularity stabilizes; tags capture intent; CF captures neighbors.

**Why Composite:** a hybrid *is* a strategy made of weighted child strategies. The rest of the pipeline does not care.

**Edge:** empty children or all-zero scores — `FallbackDecorator` still sits outside the composite.

### Decorator — `FallbackDecorator`, `DiversityDecorator`

**Problem:** two orthogonal add-ons: (1) fail open to popularity, (2) cap category monopoly.

**Why Decorator:** wrap any strategy without editing it. Fallback is a **reliability** concern; diversity is a **product** concern. They should not live inside `CollaborativeStrategy`.

**Why not inheritance (`PopularContentRanker`):** you would need a subclass for every combination.

### Chain of Responsibility — `FilterChain`

**Problem:** eligibility, hide, already-purchased, and “don’t recommend the PDP seed” are independent policies.

**Why Chain:** each filter answers `keep?`. Ban logic does not know about hide. You can insert `PriceBandFilter` later without touching CF.

**Why after scoring, not inside it:** models trained on old data will still score a SKU that was banned yesterday. Policy must be a **hard gate**.

### Builder — `RecommendationRequest`

**Problem:** 6–8 fields (`actor`, `targetUserId`, `placement`, `seed`, `limit`, exclusions). Telescoping constructors hide invalid states (PDP without seed, `limit=1_000_000`).

**Why Builder:** `build()` runs `InputValidator` once. Invalid requests never enter the facade.

### Facade — `RecommendationFacade`

**Problem:** callers would otherwise wire rate limit, authz, cache, factory, filters themselves — and skip a step.

**Why Facade:** one method `recommend()` that **always** enforces security first. Ranking is an implementation detail.

### Observer (async) — `AsyncEventBus`

**Problem:** email/push on the request thread makes p99 latency equal to the slowest notifier.

**Why async Observer:** slate generation publishes `RECS_GENERATED`; listeners fail independently. Same idea as Hotel/Amazon in this repo.

**Why not sync Observer list:** one throwing listener blanks the homepage.

### Command — `RecordInteractionCommand`

**Problem:** hide/click are writes with auth, validation, cache bust, and events. Embedding that in the UI demo couples everything.

**Why Command:** the write is an executable unit (and in production, undo/audit/outbox).

### Template-style context — `RankingContext`

**Problem:** every strategy needs request + profile + catalog + interactions + seed.

**Why a context object:** stable method signature `rank(ctx)`. Adding a field (e.g. locale) does not change every interface.

### Patterns we deliberately did **not** use

| Pattern | Why skip here |
|---------|----------------|
| Singleton catalog | Makes tests and multi-tenant demos painful; pass `Catalog` in. |
| Visitor | Overkill for a handful of rankers. |
| Mediator | Facade + bus already coordinate. |
| Prototype | Items are not cloned per request. |

---

## 3. Edge cases — what they are and how this design handles them

### Identity and authorization

| Edge case | What goes wrong | How we handle it |
|-----------|-----------------|------------------|
| Client sends `targetUserId` of another shopper | IDOR — steal taste profile | `AccessControl.requireRecommendationsFor`: self or **admin only** |
| Admin debug of Bob | Might leak Bob’s email | Admin still gets **items + reason codes only** |
| Expired / missing token | Unauthenticated personalization | `AuthService.requireUser` |
| Guest sets tags or hide | Poison / fake personalization | `requireManagePreferences` / `requireRecordInteraction` |
| Blocked account | Writes and recs continue | `AccountStatus.ACTIVE` required |

### Input and abuse

| Edge case | Handling |
|-----------|----------|
| `limit=1_000_000` | `InputValidator` clamps 1–50 (CPU DoS) |
| PDP without `seedItemId` | 400 — similar-items is undefined |
| Unknown / SQL-ish tag `drop-table` | `TagVocabulary` — must exist on a catalog item, legal charset |
| Too many tags | Cap `MAX_SELECTED = 8` |
| Tag case `Software` vs `software` | `TagNormalizer` lowercase |
| Recommend flood / scraping | Per-user `RateLimiter` |

### Ranking and product

| Edge case | Handling |
|-----------|----------|
| New user, **no tags**, no clicks | Popularity — never empty HOME |
| New user **with tags**, no clicks | `SelectedTagStrategy` blended with popularity — **not** treated as cold-start emptiness |
| User bought the only items they like | `AlreadyConsumedFilter` on HOME; popularity/tags fill the rest |
| User hid an item | `BlockedItemFilter` + cache generation bump |
| Item banned **after** it was co-purchased | `EligibilityFilter` (status is a hard gate) |
| Out of stock | Same eligibility gate |
| PDP similar-items includes the seed | `SeedItemFilter` |
| One category dominates (all electronics) | `DiversityDecorator` max 2 per category, overflow later |
| Personalization throws / returns empty | `FallbackDecorator` → popularity |
| CONTROL experiment bucket | Safer popularity+content (less CF surprise in email/holdout) |
| EMAIL placement | Same conservative blend — you cannot “uns<end” a bad email |

### Concurrency

| Edge case | Handling |
|-----------|----------|
| 16 identical HOME misses at once | Single-flight `TtlCache.getOrCompute` |
| Hide **during** an in-flight recommend | Striped `UserScopedLock` on the write; `GenerationClock` in the cache key so a late `put` is stored under a **dead** key |
| Tag replace vs recommend same user | Same lock + generation |
| Admin ban vs ranking iteration | `Item.setStatus` synchronized; rankers use `Catalog.snapshot()` |
| Interaction append vs collaborative scan | `CopyOnWriteArrayList` — readers see a consistent snapshot |
| Two tag replaces in parallel | Last writer wins **under the user lock** (serialized), not a torn set |

**Race we still name in interviews (and mitigate):**  
`recommend` computes with gen=1 → `hide` sets gen=2 → compute finishes and puts gen=1. Harmless because **nobody reads gen=1 keys anymore**. Without generations, that put would resurrect the hidden item.

### Cache

| Edge case | Handling |
|-----------|----------|
| Stale slate after hide/tags | Invalidate prefix **and** bump user generation |
| Stale slate after catalog ban | `bumpCatalog()` + `cache.clear()` (ban affects everyone) |
| TTL expiry | `TtlCache.get` drops expired entries |
| Cache returns `cached=true` | Facade wraps hits so demos/metrics can tell |

### Empty / degenerate catalog

| Edge case | Handling |
|-----------|----------|
| All items banned | Filters empty the list; fallback popularity also filters empty → empty slate (honest). Product may then show editorial merchandising (not in this LLD). |
| Seed item deleted | `catalog.require(seed)` throws — 400/404 rather than similar-to-nothing |
| User id with no profile rows | Empty affinities + empty tags → cold start popularity |

### Feedback loop (ML, not just code)

Popularity **snowballs**: shown items get clicks, clicks boost popularity. Mitigations already in the design: diversity cap, experiment CONTROL, hide. Production adds exploration (ε-greedy / bandits) and down-weights already-viral SKUs.

---

## 4. Other things worth explaining (interview and production)

### 4.1 Candidate generation vs ranking

This LLD scores the **whole in-memory catalog** because N is tiny. At scale you **must** split:

1. **Candidate generation** (offline / ANN / inverted tag index) → a few hundred ids.  
2. **Ranking** (this service) → order those ids.

Saying “we run item–item CF over 100M SKUs on the request thread” is a fail. The **class design still holds**; the factory would rank a candidate list instead of `catalog.snapshot()`.

### 4.2 Online vs offline

| Online (this facade) | Offline (jobs) |
|----------------------|----------------|
| Auth, filters, diversity, cache | Co-occurrence matrix, embeddings, popularity windows |
| Selected tags from User Service (read snapshot) | Nightly rebuild of `UserProfile` into a feature store |

`ProfileService.build()` scanning all interactions per request is demo-grade. Production snapshots features on a schedule or via a stream.

### 4.3 Why item–item CF, not user–user

More users than items in retail; item–item is stabler. **Privacy:** you never materialize “people like you.” Scores are co-counts with *the caller’s* purchases only.

### 4.4 Evaluation

- **Offline:** NDCG / recall on held-out purchases (never on items the user hid).  
- **Online:** CTR, CVR, diversity, hide-rate (guardrail).  
- **Sticky buckets:** `ExperimentAssigner` hashes `userId` so the same human does not flip CONTROL/TREATMENT every request (that invalidates the A/B).

### 4.5 GDPR / CCPA (privacy ops, not just IDOR)

Selected tags and clicks are **profiling**. Handle:

- Hide = “do not use this item as a signal or candidate.”  
- Delete user = wipe interactions + tags + cache keys (generation bump is not enough; you need a delete).  
- Do not log emails next to slates. We log `userId` + tag names for the owner in the demo notify line — production should sample or hash.

### 4.6 Security recap (why ranking must not weaken it)

| Control | Why it belongs in recs |
|---------|------------------------|
| Salted password, session TTL | Personalized recs are PII-adjacent |
| Opaque token, not `userId` as auth | Classic IDOR |
| Vocabulary-checked tags | Feature injection |
| Rate limits | Scoring is expensive; recs leak catalog shape |
| Generic reason codes | “Because user X bought this” is a data leak |

Passwords here are SHA-256+salt (**demo**). Production: Argon2id / BCrypt.

### 4.7 Consistency model (say this out loud)

Recommendation is **not** a banking ledger. We choose:

- **Read-your-writes** for hide/tags: lock + generation so the next recommend is fresh.  
- **Eventual** for “Charlie’s purchase should affect Alice’s CF” — a few seconds late is OK; Kafka + periodic matrix rebuild.

Do not put a global lock around all recommends to make CF perfectly linearizable. Throughput dies; product does not need it.

### 4.8 What I would add next (not in this LLD)

- Single-flight + Redis GET/SET with `userId:placement:gen`.  
- Circuit breaker around a remote model HTTP call (Decorator already matches that shape).  
- Locale / currency as part of the cache key.  
- Exploration arm on HOME only (never EMAIL).  
- Per-placement filter chains (EMAIL might exclude low-margin SKUs).  
- Outbox for interaction events so cache invalidation survives process crash.  
- Keep `TAG` reason code through hybrid (today hybrid overwrites to `HYBRID` — scores still reflect tags; explainability is coarser).

### 4.9 How to talk through it in 10 minutes

1. **Requirements:** placements, tags, cold start, hide, no PII.  
2. **Pipeline:** security → profile → strategy factory → filters → diversity.  
3. **Patterns:** Strategy/Factory for rankers, Chain for policy, Decorator for fallback, Facade as the door.  
4. **Tags:** User Service snapshot is a first-class Strategy, not a click side-effect.  
5. **Concurrency:** stripe writes, generation cache keys, single-flight.  
6. **Scale punchline:** this is the ranker; candidates come from an index.

---

## 5. Map: thought → class

| Thought | Class / type |
|---------|----------------|
| “Don’t trust client userId” | `AuthService`, `AccessControl` |
| “Tags are User Service, not clicks” | `UserPreferenceService`, `SelectedTagStrategy` |
| “Don’t store junk tags” | `TagVocabulary`, `TagNormalizer` |
| “HOME ≠ PDP” | `Placement`, `RankingStrategyFactory` |
| “Ban is policy” | `EligibilityFilter` |
| “Empty page is a sev-1” | `FallbackDecorator` |
| “Hide must win the race” | `GenerationClock`, `UserScopedLock` |
| “16 parallel first loads” | `TtlCache.getOrCompute` |
| “Don’t block on email” | `AsyncEventBus` |

If you remember only one sentence: **rankers propose, filters dispose, the facade never skips the door.**
