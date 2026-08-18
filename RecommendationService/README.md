# Recommendation Service LLD

Low-level design of a **personalized product recommendation service** that addresses common interview pitfalls (one giant popularity list, leaking neighbor identity, no cold-start, sync scoring on the request thread, missing hide/ban filters, client-supplied userId as “auth”).

## Features Required

- **Placements:** `HOME`, `PRODUCT_DETAIL` (similar items), `CART`, `EMAIL`.
- **Cold start:** guests and new members with **no tags and no history** get popularity.
- **User Service tags:** members select catalog tags; `SelectedTagStrategy` ranks matching items even with zero clicks.
- **Personalization:** content-based (inferred) + selected tags (explicit) + item–item collaborative.
- **Hybrid ranking:** weighted ensemble; experiment **CONTROL** stays on safer popularity/content.
- **Feedback loop:** view / click / like / purchase / dislike / hide (Command).
- **Eligibility:** banned and out-of-stock SKUs are hard-filtered; purchases excluded on HOME.
- **Diversity:** cap items per category so one affinity does not dominate.
- **Explainability:** generic reason codes (`POPULAR`, `CONTENT`, `COLLABORATIVE`, `SIMILAR`, `TAG`, `HYBRID`).
- **Caching:** TTL + **single-flight**; cache keys include user/catalog **generations**.
- **Concurrency:** striped per-user locks on tag/interaction writes; catalog snapshots while ranking.
- **Notifications:** async event bus on slate generation and feedback.
- **Security:** salted passwords, session tokens, IDOR checks, rate limits, input bounds, no PII in responses.

## Package structure

```
com.reco.lld
├── account/     User, AuthService, Session, AccessControl, PasswordUtils
├── catalog/     Item, Catalog, Category, ItemStatus
├── profile/     Interaction*, UserProfile, ProfileService
├── userservice/ UserPreferenceService, TagVocabulary
├── concurrency/ UserScopedLock, GenerationClock
├── ranking/     RankingStrategy*, SelectedTagStrategy, Factory, Hybrid, Decorators
├── pipeline/    FilterChain (eligibility, seed, blocked, purchased)
├── request/     RecommendationRequest (Builder), Response, Placement
├── security/    RateLimiter, InputValidator
├── experiment/  ExperimentAssigner (CONTROL / TREATMENT)
├── cache/       TtlCache
├── events/      AsyncEventBus, NotificationService
├── command/     RecordInteractionCommand
├── service/     RecommendationFacade, InteractionService, CatalogAdmin
└── demo/        RecommendationService + *Scenario demos
```

## Run

```bash
cd RecommendationService
javac -d out $(find src -name '*.java')
java -cp out com.reco.lld.demo.RecommendationService              # all
java -cp out com.reco.lld.demo.RecommendationService list         # names
java -cp out com.reco.lld.demo.RecommendationService personalize  # one
```

Available scenarios: `auth`, `access`, `coldstart`, `tags`, `personalize`, `similar`, `feedback`, `filter`, `rate`, `experiment`, `notify`, `concurrent`.

## Problems → Solutions

| # | Common mistake | Fix in this codebase |
|---|----------------|----------------------|
| 1 | One hardcoded popularity list | Strategy + Factory by placement / experiment / cold-start |
| 2 | “Users like you” leaks neighbor ids | Collaborative scores only; reason code `COLLABORATIVE` |
| 3 | Client sends `userId` as identity | Opaque session token + IDOR: actor must match target (or admin) |
| 4 | Banned items still recommended | `EligibilityFilter` after scoring |
| 5 | Personalization outage → empty page | `FallbackDecorator` → popularity |
| 6 | Selected tags ignored / cold-start popularity | `UserPreferenceService` + `SelectedTagStrategy` |
| 7 | Stale cache after concurrent hide/tag | GenerationClock in cache key + single-flight |

## Core flow

```
Client ──Bearer token──► AuthService.requireUser
       ──recommend──► RecommendationFacade
                         ├── RateLimiter
                         ├── AccessControl (no IDOR)
                         ├── TtlCache
                         ├── ProfileService (selected tags + affinities)
                         ├── RankingStrategyFactory (incl. SelectedTagStrategy)
                         ├── FilterChain
                         ├── DiversityDecorator
                         └── AsyncEventBus → NotificationService
Client ──set tags──► UserPreferenceService (vocabulary check)
                         └── bump generation + invalidate cache
Client ──hide/click──► RecordInteractionCommand → InteractionService
                         └── striped lock + bump generation + invalidate
```

## Patterns used

- **Strategy** — popularity, content, collaborative, similar-items, **selected tags**  
- **Factory Method** — `RankingStrategyFactory`  
- **Composite** — `HybridRankingStrategy`  
- **Decorator** — fallback + diversity  
- **Chain of Responsibility** — post-rank filters  
- **Builder** — `RecommendationRequest`  
- **Facade** — `RecommendationFacade`  
- **Observer (async)** — `AsyncEventBus`  
- **Command** — record interaction  
- **Template-style context** — `RankingContext` shared by rankers  

## Docs

- `HLD.md` — architecture, tech choices, components  
- `README.md` — this file  
- `DESIGN_THOUGHT_PROCESS.md` — **why this design**, patterns, edge cases  
- `CLASS_AND_DATA_MODEL.md` — classes and tables  
- `API_REFERENCE.md` — REST-style contracts  
- `PROBLEMS_AND_SOLUTIONS.md` — interview pitfalls  
- `INTERVIEW_QUESTIONS.md` — follow-ups  

## Notes

This is an LLD teaching / interview codebase — Redis, Kafka, feature-store, and real CF (ALS / two-tower) are stubbed as in-memory maps and co-occurrence counts.
