# Spotify LLD — Interview Prep Guide

Read this before the interview. It covers **what to build**, **patterns to name**, **concepts to defend**, and **trap answers** interviewers expect.

---

## 1. What Are We Building?

A **music streaming platform** (Spotify-like) with:

| Feature | What it means in LLD |
|---------|----------------------|
| Auth & registration | Salted hashes + session tokens (never plaintext passwords) |
| Catalog & search | Indexed lookup (Trie), not scan-a-list |
| Streaming | Chunks → buffer → decoder (not `song.play()`) |
| Playlists | Composite tree with cycle/depth guards + thread-safe edits |
| Recommendations | Event-driven affinity (PLAY / SKIP / LIKE / REPEAT) |
| Social | Directed follow graph + playlist visibility ACL |
| Offline | Device-bound, time-bound, encrypted downloads |
| Licensing | Country + validity window before every play |
| Abuse protection | Concurrent stream cap + API rate limit |

**One-liner you can say:**  
> “I’m designing the in-process LLD of a streaming client/service: per-session playback, licensed streaming, indexed catalog, social ACL, and async notifications — not a global singleton player.”

---

## 2. Design Patterns Used

| Pattern | Where | Why say it in interview |
|---------|-------|-------------------------|
| **Singleton (scoped correctly)** | Prefer *service* singletons (`SessionManager`, `AuthService`, catalog) — **not** one `MusicPlayer` for the whole platform | Interviewers fail “global MusicPlayer singleton” |
| **Session / Context** | `User` → `PlaybackSession` → `MusicPlayer` + `PlayerState` | One player **per device/session** |
| **Composite** | `Playlist implements Music`, can nest playlists | Nested playlists; must add **cycle detection + depth limit** |
| **Observer (async)** | `AsyncEventBus` + `MusicEventListener` | Friend liked / new release / playlist updated — **async fan-out** |
| **Strategy / Policy** | `PlaylistAccessControl` on `PUBLIC \| FOLLOWERS_ONLY \| PRIVATE` | Visibility rules without hardcoding in `Playlist` |
| **Facade / Service** | `LicenseService`, `AuthService`, `DownloadManager` | Hide hashing, license maps, offline DRM checks |
| **Producer–Consumer** | `StreamingPlayer` pulls chunks from `StreamBuffer` | Backpressure / pacing between buffer and decoder |

**Patterns often claimed but weak if missing implementation:** MVC, Factory, Decorator. If asked, say:  
> “Factory for creating Song/Album/Playlist types and Decorator for lyrics/artwork are natural extensions; this codebase focuses on the failure modes that break production designs.”

---

## 3. Core Concepts (Must Be Able to Explain)

### Concurrency
- **Race on lazy singleton** → double-checked locking / enum / holder — or better: **don’t use a global player**.
- **Playlist mutation during play** → `CopyOnWriteArrayList` (or snapshot + lock); edits + playback are concurrent in real apps.
- **CAS stream limit** → `AtomicInteger.compareAndSet` so two devices can’t both sneak under the max concurrent streams.

### Streaming vs playback
```
Track bytes → AudioStreamBuffer (chunks) → StreamingPlayer → AudioDecoder
```
Saying `music.play()` is **UI/domain playback**, not a streaming architecture. Real systems need chunking, buffering, and backpressure.

### Licensing = legal constraint
```
canPlay = country ∈ allowedCountries  AND  now ∈ [validFrom, validUntil]
```
Example: block Queen in `IN`, allow in `US`. Call **before** stream starts.

### Auth
- Store `salt + hash(password, salt)` — never `String password`.
- Login issues **AuthToken** with expiry; API uses token, not password.
- Production: BCrypt/Argon2 + JWT/refresh tokens (mention as next step).

### Search at scale
- Linear `for (Song s : list)` is **O(n)** — dies at tens of millions of tracks.
- **Trie / inverted index**: prefix/partial search in ~**O(k)** on query length.
- This project: suffix-indexed Trie for title & artist.

### Recommendations are event-driven
Events: `PLAY`, `SKIP`, `LIKE`, `REPEAT`, `ADD_TO_PLAYLIST`, `SHARE`  
→ update affinity scores → rank for user. Cold start → genre/popularity charts.

### Social graph
```
User --follows--> User
Playlist.visibility ∈ { PUBLIC, FOLLOWERS_ONLY, PRIVATE }
```
“Can Bob see Alice’s playlist?” = ACL(viewer, owner, visibility, follow edge).

### Offline ≠ save file
Constraints (Spotify-like talking points):
- **Time-bound** (e.g. 30 days)
- **Device-bound**
- **Encrypted** + license/DRM token  
Without these, “offline mode” is a wishlist.

### Limits & protection
- Concurrent streams per account (e.g. free = 1).
- Token-bucket rate limit per user on APIs.
- Without limits, stolen accounts / abusive clients scale damage infinitely.

---

## 4. Problem → Trap → Your Answer

| # | Interview trap | Strong answer |
|---|----------------|---------------|
| 1 | Unsynchronized `if (instance == null)` | Race → multiple players; use enum/sync **or** per-session player |
| 2 | One `MusicPlayer` for all users | Shared state; User A pause stops User B |
| 3 | Plain `ArrayList` playlist | `ConcurrentModificationException` under concurrent play/edit |
| 4 | Nested playlists with no cycle check | `StackOverflowError` on `play()` |
| 5 | Only `song.play()` | That’s playback glue, not streaming |
| 6 | Offline = local path | Need expiry, device bind, encryption |
| 7 | No geo license | Labels sue; model country + window |
| 8 | `getRecommendations()` with no events | Recs need listen-event stream + ranking |
| 9 | Search by looping list | Index (Trie / ES) |
| 10 | Plaintext password | Instant reject; salt+hash+token |
| 11 | “Follow users” with no graph | Directed graph + visibility |
| 12 | “We use Observer” with no bus | Async publish/subscribe + fan-out |
| 13 | Unlimited streams/API | CAS limiter + token bucket |

---

## 5. Package & Class Map (Quick Mental Model)

```
com.spotify.lld.auth            User, PasswordUtils, AuthToken, AuthService
com.spotify.lld.playback        SessionManager, PlaybackSession, MusicPlayer, PlayerState
com.spotify.lld.streaming       StreamBuffer, AudioStreamBuffer, StreamingPlayer, AudioDecoder
com.spotify.lld.catalog         Music, Song, MusicCatalog, CatalogSearchIndex, TrieNode
com.spotify.lld.playlist        Playlist, PlaylistVisibility
com.spotify.lld.license         License, LicenseService, LicenseException
com.spotify.lld.offline         DownloadManager, OfflineTrack
com.spotify.lld.social          SocialGraph, PlaylistAccessControl
com.spotify.lld.events          AsyncEventBus, MusicEvent, MusicEventListener, NotificationService
com.spotify.lld.recommendation  ListenEvent, ListenEventType, RecommendationEngine
com.spotify.lld.limits          StreamLimiter, RateLimiter, TokenBucket
com.spotify.lld.demo            MusicStreamingService + feature scenarios
```

**Playback hierarchy to draw on whiteboard:**
```
SessionManager
  └── PlaybackSession (per user/device)
        ├── PlayerState
        └── MusicPlayer
              └── StreamingPlayer + StreamBuffer
```

---

## 6. Whiteboard Flow (60-Second Version)

1. User registers/logs in → hashed password → token.  
2. Create `PlaybackSession`.  
3. User picks track → `LicenseService.assertPlayable(track, user.country)`.  
4. `StreamLimiter.tryAcquire` + `RateLimiter.tryConsume`.  
5. Stream chunks through buffer/decoder; emit `ListenEvent`s.  
6. Event bus notifies friends / updates recommendations.  
7. Optional: download offline with device + expiry + token.

---

## 7. Trade-offs & Extensions (Impress Follow-ups)

| Topic | Current LLD choice | Production next step |
|-------|--------------------|----------------------|
| Password hash | SHA-256 + salt (demo) | Argon2id / BCrypt |
| Search | In-memory Trie | Elasticsearch / OpenSearch |
| Events | In-process thread pool | Kafka / SNS + consumer groups |
| DRM | Stub encrypt + token | Widevine / FairPlay + KMS keys |
| Stream limit | In-memory CAS map | Redis atomic counters (multi-node) |
| Playlist concurrency | CopyOnWrite | Fine-grained locks or CRDT for collab playlists |
| Recs | Simple affinity scores | Collaborative filtering + embeddings |

**Solid closer:**  
> “This LLD proves correctness of session isolation, licensing, indexing, and abuse limits in one process. At scale I’d move session/stream counters and the event bus to shared infra, but the **domain boundaries stay the same**.”

---

## 8. How to Run Demos Before the Interview

```bash
javac -d out $(find src -name '*.java')
java -cp out com.spotify.lld.demo.MusicStreamingService list
java -cp out com.spotify.lld.demo.MusicStreamingService auth      # tokens
java -cp out com.spotify.lld.demo.MusicStreamingService session   # per-user players
java -cp out com.spotify.lld.demo.MusicStreamingService playlist  # cycle guard
java -cp out com.spotify.lld.demo.MusicStreamingService streaming # chunks
java -cp out com.spotify.lld.demo.MusicStreamingService license   # geo block
java -cp out com.spotify.lld.demo.MusicStreamingService limits    # CAS + token bucket
```

Walk one scenario aloud; tie it back to the problem number in section 4.

---

## 9. Cheat Sheet — Sentences to Memorize

1. **Player is per session, not global.**  
2. **Composite playlists need cycle detection.**  
3. **Streaming = chunks + buffer + decoder.**  
4. **License = location + time window.**  
5. **Offline = encrypted + device-bound + expiring.**  
6. **Search must be indexed.**  
7. **Recommendations are driven by listen events.**  
8. **Passwords are salted hashes; sessions are tokens.**  
9. **Follow graph + visibility = social ACL.**  
10. **Observer must be async and scalable.**  
11. **Without stream/rate limits, abuse is guaranteed.**

---

*Companion overview of the repo layout: see `README.md`.*  
*Senior (5+ YOE) interview questions: see `INTERVIEW_QUESTIONS.md`.*
