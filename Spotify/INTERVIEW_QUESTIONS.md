# Spotify LLD — Interview Questions (5+ YOE)

Senior / SDE-2 style questions around this music streaming LLD. Focus on **depth, trade-offs, and failure modes** — not pattern name-dropping.

Companion: [`INTERVIEW_PREP_GUIDE.md`](./INTERVIEW_PREP_GUIDE.md) (patterns, trap answers, cheat sheet).

---

## 1. Architecture & Ownership

1. Walk me through the domain boundaries of your Spotify LLD. What stays in-process vs what you’d move out first at scale?
2. Why is a global `MusicPlayer` singleton wrong even if you make it thread-safe?
3. How would you draw the object graph for: one user, two devices, both playing different tracks?
4. What’s the difference between *domain playback* (`play/pause`) and *streaming* in your design?
5. If this LLD becomes a service, where do you put auth, licensing, and stream limits — same service or separate?

---

## 2. Concurrency

6. `CopyOnWriteArrayList` for playlists — when is it a good fit, and when does write amplification kill you?
7. Two threads: one plays a playlist, one mutates it. What races remain even with COW?
8. How does `StreamLimiter` with CAS prevent double-acquire under contention? Walk through a failed CAS retry.
9. Token bucket vs sliding window vs leaky bucket — why token bucket for API rate limits here?
10. Your `AsyncEventBus` drops or backs up under load. How do you design for backpressure without losing critical events?
11. Session creation is concurrent. How do you avoid two sessions for the same device fighting over stream slots?

---

## 3. Design Patterns — with Traps

12. Composite for nested playlists: how do you detect cycles, and what’s the complexity of your check?
13. Why depth limits *and* cycle detection? Can one exist without the other?
14. Strategy for playlist ACL vs putting `if (visibility == …)` inside `Playlist` — defend the split.
15. Observer as async bus: sync Observer would be simpler — what breaks in production if listeners are sync?
16. Where would Factory / Decorator actually earn their keep in this system, and where are they cargo-cult?

---

## 4. Auth & Security

17. Salt + hash is necessary but not sufficient. What else do you need for production auth at Spotify scale?
18. Token expiry alone — how do you handle logout, device revoke, and stolen refresh tokens?
19. Where should password hashing run (client / API / worker), and why?
20. How do you design auth so a compromised read-replica DB doesn’t leak usable credentials?

---

## 5. Catalog & Search

21. Why is linear scan unacceptable past ~10M tracks? Quantify latency if you can.
22. Trie vs inverted index vs Elasticsearch — what does each optimize for (prefix, fuzzy, ranking)?
23. How do you keep a search index consistent when titles/artists are renamed concurrently with searches?
24. Suffix indexing for partial match — memory cost vs query benefit. When do you stop using an in-memory Trie?

---

## 6. Streaming & Playback

25. Design the buffer: fixed chunk size, watermark for pause/resume, and what happens on network stall.
26. Producer–consumer between buffer and decoder — who applies backpressure, and how?
27. Seek / skip mid-track: what state do you invalidate in buffer + session?
28. How do free vs premium tiers change buffer size, bitrate ladder, and concurrent stream policy?

---

## 7. Licensing & Offline

29. Model geo + time-window licensing. What happens if the user’s VPN country ≠ account country?
30. License check before stream starts vs continuous re-check — trade-offs?
31. Offline is not “save file.” Defend device-bind, expiry, and encryption as *required* constraints.
32. Offline license expires while the user is on a plane. What’s the product vs legal trade-off?
33. How would you rotate DRM keys without forcing every user to re-download?

---

## 8. Social, ACL & Recommendations

34. Directed follow graph: can Alice follow Bob without Bob following Alice? How does `FOLLOWERS_ONLY` use that?
35. Collaborative playlist edits from two followers — last-write-wins, OT, or CRDT? Pick and justify.
36. Recommendations from listen events (`PLAY/SKIP/LIKE/REPEAT`) — how do you weight them, and how do you fight popularity bias?
37. Cold start for a new user — what signals do you use before they have history?
38. Eventual consistency: friend gets a “liked” notification after the like was undone. How do you handle it?

---

## 9. Limits, Abuse & Multi-node

39. In-memory CAS stream limit works on one JVM. How do you enforce “1 concurrent stream” across 50 pods?
40. Stolen account sharing passwords across cities — what signals + limits would you add beyond stream count?
41. Rate limiter keys: per user, per IP, per device, per endpoint — how do you compose them without locking out NAT users?
42. What happens when Redis (for counters) is down — fail open or fail closed for stream limits? Defend it.

---

## 10. Senior Trade-off / System Thinking

43. Convert this LLD into HLD: name services, data stores, and the critical path for “press play.”
44. Playlist is both a *document* and a *queue for playback*. How do you model that without coupling?
45. You’re asked to add podcasts + audiobooks. What abstractions stay, what splits?
46. Biggest correctness bug you’d expect a mid-level engineer to ship in this design — and how you’d catch it in review.
47. Metrics and SLOs: what do you measure for play-start latency, buffer underruns, and license denials?
48. How would you test cycle detection, CAS limiter, and ACL in a way that survives refactoring?

---

## How Interviewers Score 5+ YOE

| Signal | Weak | Strong |
|--------|------|--------|
| Patterns | Names them | Ties to failure mode + trade-off |
| Concurrency | “Use synchronized” | COW vs locks, CAS, multi-node |
| Streaming | `song.play()` | Chunks, buffer, backpressure |
| Scale | “Add Kafka” | What moves, what stays domain-local |
| Offline/License | Feature list | Legal + device + expiry constraints |

---

## 8 Questions You Should Answer Cold

1. Player is **per session**, not global.
2. Nested playlists need **cycle + depth** guards.
3. Streaming = **chunks → buffer → decoder**.
4. License = **country ∩ validity window**.
5. Offline = **encrypted + device-bound + expiring**.
6. Search must be **indexed**, not scanned.
7. Recs are driven by a **listen-event stream**.
8. Without **stream + rate limits**, abuse is unbounded.
