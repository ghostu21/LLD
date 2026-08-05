# Spotify LLD — Problems Faced & Solutions

Each item is a common LLD interview pitfall: **what goes wrong**, a **concrete example**, how **this codebase fixes it**, and what the **interviewer is really probing**.

Companions: [`INTERVIEW_PREP_GUIDE.md`](./INTERVIEW_PREP_GUIDE.md) · [`INTERVIEW_QUESTIONS.md`](./INTERVIEW_QUESTIONS.md)

---

## Quick Map

| # | Problem | Fix |
|---|---------|-----|
| 1 | Lazy singleton `MusicPlayer` not thread-safe | Prefer no global player; session-scoped state |
| 2 | One global player for all users | `SessionManager` → `PlaybackSession` → `MusicPlayer` |
| 3 | `ArrayList` playlist under concurrent edit/play | `CopyOnWriteArrayList` |
| 4 | Nested playlists → cycle → `StackOverflowError` | Cycle detection + max nesting depth |
| 5 | `song.play()` treated as streaming | Chunks → buffer → decoder |
| 6 | Offline = “save file” | Device-bound + expiry + encryption + license |
| 7 | No geo/time licensing | Country ∩ validity window before play |
| 8 | Recommendations as a method call | Listen-event stream → affinity ranking |
| 9 | Search by scanning `List<Song>` | Trie / indexed catalog |
| 10 | Plaintext password | Salt + hash + expiring token |
| 11 | Follow/share with no graph/ACL | Directed graph + visibility ACL |
| 12 | Claimed Observer, no events | Async event bus + fan-out |
| 13 | No stream/API limits | CAS stream limiter + token bucket |

---

## 1. Singleton `MusicPlayer` Is Not Thread-Safe

### Problem
Naive lazy singleton:

```java
public static MusicPlayer getInstance() {
    if (instance == null) {
        instance = new MusicPlayer();  // race here
    }
    return instance;
}
```

Two threads can both see `instance == null` and create two players. Singleton guarantee is broken; playback state races.

### Example
```
T1 and T2 call getInstance() at the same time
  → two MusicPlayer objects
  → T1 starts Song A, T2 starts Song B on "the" player
  → volume / position / currentTrack overwrite each other
```

### Solution
Do **not** stop at `synchronized` / double-checked locking. Playback state should not be a platform-wide singleton at all (see Problem 2).  
Service-level singletons (`AuthService`, catalog) can use enum / holder idiom if needed — never a shared `MusicPlayer`.

**Code:** `playback/SessionManager.java`, `playback/PlaybackSession.java`

### Interviewer perspective
They are checking whether you understand **races on lazy init**, and whether you blindly “fix Singleton” or question whether Singleton belongs here. Strong answer: name the race *and* say the real fix is per-session ownership.

---

## 2. Global MusicPlayer Is a Wrong Abstraction

### Problem
Even a perfectly thread-safe global player is wrong: one player for all users. User A’s pause stops User B. Multi-device / multi-user is impossible.

### Example
```
Alice (phone)  → play("Bohemian Rhapsody")
Bob   (laptop) → pause()   // hits the same global player
Alice’s music stops. Alice blames “a bug.”
```

Correct mental model:

```
SessionManager
  ├── PlaybackSession(Alice, phone)  → MusicPlayer + PlayerState
  └── PlaybackSession(Bob, laptop)   → MusicPlayer + PlayerState
```

### Solution
`SessionManager.createSession(user)` gives each session its own `MusicPlayer` and `PlayerState`.

**Code:** `playback/SessionManager.java`, `playback/PlaybackSession.java`  
**Demo:** `session`

### Interviewer perspective
This separates **junior pattern-name answers** from **senior domain modeling**. They want: “player is per device/session, not per JVM.” Bonus if you mention free-tier concurrent stream caps sitting *above* sessions (Problem 13).

---

## 3. Playlist Is Not Thread-Safe

### Problem
```java
private List<Music> songs = new ArrayList<>();
```

Play iterates the list while another thread adds/removes → `ConcurrentModificationException`, skipped tracks, corrupted order.

### Example
```
Thread A: for (song : playlist) { play(song); }   // iterating
Thread B: playlist.addSong(newSong);              // structural mod
→ ConcurrentModificationException mid-play
```

Real products: user edits a playlist while it’s playing or while a collaborator syncs.

### Solution
`CopyOnWriteArrayList` — iterators see a stable snapshot; writes copy-on-write (good for read-heavy playlists). For write-heavy collaborative playlists, prefer fine-grained locks or CRDTs at scale.

**Code:** `playlist/Playlist.java`  
**Demo:** `playlist`

### Interviewer perspective
They want you to **spot concurrent readers/writers** without being told. Mentioning COW *and* when COW is wrong (high write rate) shows 5+ YOE judgment.

---

## 4. Composite Misused — Cycles & Infinite Recursion

### Problem
`Playlist implements Music` so playlists can nest. Without guards:

```
Workout → Chill → Workout   // cycle
play(Workout) → StackOverflowError
```

### Example
```java
Playlist workout = new Playlist("Workout", "u1");
Playlist chill   = new Playlist("Chill", "u1");
workout.addSong(chill);
chill.addSong(workout);   // must throw — would create a cycle
```

Also deep nesting (playlist in playlist × 1000) can blow the stack even without a cycle.

### Solution
- Reject self-add and cycle-creating adds (`wouldCreateCycle`)
- Cap nesting (`MAX_NESTING_DEPTH = 10`)
- Track visited playlists during `play()` as a second line of defense

**Code:** `playlist/Playlist.java`  
**Demo:** `playlist`

### Interviewer perspective
Anyone can say “Composite.” Seniors say **“Composite over graphs needs cycle + depth guards.”** Expect a whiteboard of A→B→A and where you check.

---

## 5. No Streaming Model (`song.play()` ≠ Streaming)

### Problem
```java
song.play();  // prints a line — not streaming
```

Missing: chunking, buffering, backpressure, (later) bitrate adaptation.

### Example
```
Bad:  song.play()

Good: Track bytes
        → split into chunks
        → AudioStreamBuffer
        → StreamingPlayer pulls chunks
        → AudioDecoder.decode(chunk)
```

Network stall: buffer underruns → player waits / retries instead of assuming the whole file is local.

### Solution
Producer–consumer between buffer and decoder; player paces consumption (`StreamingPlayer`).

**Code:** `streaming/AudioStreamBuffer.java`, `streaming/StreamingPlayer.java`, `streaming/AudioDecoder.java`  
**Demo:** `streaming`

### Interviewer perspective
They are filtering candidates who design **UI playback** vs **delivery pipeline**. Say the interview line clearly: *“That’s playback glue, not a streaming architecture.”* Mention buffering + backpressure even if bitrate ladder is out of scope for LLD.

---

## 6. Offline Mode Not Designed

### Problem
“Offline supported” with no DRM, encryption, expiry, or device binding. Saving a file is a wishlist, not a design.

### Example
```
Download BohemianRhapsody.mp3 to /Downloads
  → user copies file to USB
  → subscription lapses, file still plays forever
  → labels sue
```

Spotify-like rules:

| Constraint | Example |
|------------|---------|
| Time-bound | Re-auth / refresh every ~30 days |
| Device-bound | Playable only on the device that downloaded |
| Encrypted | File useless without license/key |

### Solution
`DownloadManager.download(...)` stores encrypted payload + `OfflineTrack(deviceId, expiry, licenseToken)`. `getPlayableTrack` checks device + expiry before play.

**Code:** `offline/DownloadManager.java`, `offline/OfflineTrack.java`  
**Demo:** `offline`

### Interviewer perspective
They test whether you treat offline as **product + legal**, not filesystem. Strong close: “Offline without expiry and device bind is piracy with extra steps.”

---

## 7. No Licensing / Rights Enforcement

### Problem
Licensing claimed, but no country rules or validity windows. Streaming without a valid license is a lawsuit, not a feature bug.

### Example
```
Queen track licensed for US: 2024-01-01 → 2026-12-31
User in IN tries play → LicenseException
User in US tries play → OK
License expires 2027-01-01 → US also blocked
```

Constraint:

```
canPlay = country ∈ allowedCountries  AND  now ∈ [validFrom, validUntil]
```

### Solution
Register licenses; call `LicenseService.assertPlayable(trackId, userCountry)` **before** starting the stream.

**Code:** `license/License.java`, `license/LicenseService.java`  
**Demo:** `license`

### Interviewer perspective
Classic probe: *“Block in India, allow in US — how?”* They want a **data model** (countries + window), not “if country == IN.” Mention VPN/geo ambiguity as a follow-up trade-off if they push.

---

## 8. Recommendations Not Designed

### Problem
`getRecommendations(user)` with no events, ranking, or cold-start story. Recommendations are an **event-driven system**, not a method stub.

### Example
```
Events for Alice:
  PLAY  SongA   → +affinity(A)
  SKIP  SongB   → −affinity(B) or ignore after N skips
  LIKE  SongC   → strong +affinity(C)
  REPEAT SongA  → strong +affinity(A)

Rank catalog by affinity → top-N for Alice
New user (cold start) → genre seeds / global charts until events exist
```

### Solution
`ListenEvent` + `ListenEventType` feed `RecommendationEngine` affinity scores.

**Code:** `recommendation/ListenEvent.java`, `recommendation/RecommendationEngine.java`  
**Demo:** `recommend`

### Interviewer perspective
They reject “ML magic” without an **input stream**. Name events, weights, and cold start. At 5+ YOE, mention popularity bias and that production moves events to Kafka + offline training.

---

## 9. No Search / Catalog Indexing

### Problem
```java
for (Song s : allSongs) if (s.getTitle().contains(q)) ...
```
**O(n)** over tens of millions of tracks → timeouts.

### Example
```
Catalog size: 100,000,000 tracks
Query: "bohem"

Linear scan: check every title          → seconds+
Trie / inverted index: walk query chars → ~O(k) on query length
```

Need: artist, album, partial text, later genre filters — all index-backed.

### Solution
Suffix-oriented Trie in `CatalogSearchIndex` for prefix/partial match. Production: Elasticsearch / OpenSearch.

**Code:** `catalog/CatalogSearchIndex.java`, `catalog/TrieNode.java`  
**Demo:** `catalog`

### Interviewer perspective
They want you to refuse list-scan without prompting. Quantify (“O(n) dies at catalog scale”) and name the index type. Memory cost of Tries vs ES is a good senior follow-up.

---

## 10. Authentication Plainly Unsafe

### Problem
```java
private String password;  // plaintext — instant reject
```

No hash, no salt, no session token → DB leak = every account owned.

### Example
```
Bad:  store password = "Mayank@123"
Good: store salt + hash(password, salt)
Login: verify hash → issue AuthToken(expiry)
API:  Authorization: <token>  (not password again)
Logout / expiry: token invalidated
```

### Solution
`PasswordUtils` salt+hash · `AuthService.login` issues `AuthToken` · `validateToken` / `logout`. Demo uses SHA-256+salt; production → Argon2id/BCrypt + refresh tokens.

**Code:** `auth/AuthService.java`, `auth/AuthToken.java`, `auth/PasswordUtils.java`  
**Demo:** `auth`

### Interviewer perspective
Plaintext is a **hard fail**, not a nit. They listen for salt, hash, token expiry, and “password never stored or logged.” Naming Argon2/JWT as next step shows production awareness without overbuilding the LLD.

---

## 11. No Social Graph / ACL

### Problem
“Follow users / share playlists” without edges or visibility. Cannot answer: who follows whom? Can Bob see Alice’s playlist?

### Example
```
Alice --follows--> Bob
Bob does NOT follow Alice

Alice playlist "Gym":
  PUBLIC          → everyone can view
  FOLLOWERS_ONLY  → only users who follow Alice (Bob cannot see Gym)
  PRIVATE         → only Alice

Bob playlist "Jazz" FOLLOWERS_ONLY:
  Alice follows Bob → Alice can view Jazz
```

### Solution
`SocialGraph` (directed follows) + `PlaylistVisibility` + `PlaylistAccessControl.canView(viewer, playlist)`.

**Code:** `social/SocialGraph.java`, `social/PlaylistAccessControl.java`  
**Demo:** `social`

### Interviewer perspective
Social features without ACL are a **privacy / trust fail**. They want a directed graph + explicit visibility enum, and a clear `canView` decision table like the example above.

---

## 12. Observer Claimed, Event System Missing

### Problem
Design doc says “Observer Pattern” but nothing publishes or fans out. No notifications for likes, releases, playlist updates.

### Example
```
Alice likes SongX
  → MusicEvent(LIKE, Alice, SongX)
  → AsyncEventBus.publish
  → NotificationService notifies Alice’s followers
  → RecommendationEngine may also listen

If listeners run on the publish thread:
  slow email / push → blocks play path  → bad UX
→ Observer must be async
```

### Solution
`AsyncEventBus` + `MusicEventListener` + `NotificationService`. Production: Kafka/SNS + consumer groups.

**Code:** `events/AsyncEventBus.java`, `events/NotificationService.java`  
**Demo:** `events`

### Interviewer perspective
Pattern name without **pub/sub + async fan-out** is an empty claim. They ask: “What happens when 1M followers get a new-release event?” — push you toward queues and backpressure (Problem 13 territory).

---

## 13. No Metrics, Limits, or Protection

### Problem
No concurrent stream cap, quotas, or rate limits. Stolen/shared accounts and abusive clients scale damage without bound.

### Example
```
Free tier: maxConcurrentStreams = 1

Alice already streaming on phone
Laptop calls tryAcquireStream("alice") → false (blocked)

CAS under contention:
  both devices read count=0, both try count=1
  only one compareAndSet succeeds; other retries and sees limit

API abuse:
  token bucket capacity=10, refill=10/sec
  client bursts 50 req/s → after 10, tryConsume() == false
```

Rule of thumb: **without limits, abuse is guaranteed.**

### Solution
`StreamLimiter` (CAS on `AtomicInteger`) + `RateLimiter` / `TokenBucket`. Multi-node: move counters to Redis.

**Code:** `limits/StreamLimiter.java`, `limits/RateLimiter.java`, `limits/TokenBucket.java`  
**Demo:** `limits`

### Interviewer perspective
They test **abuse and multi-threaded correctness**, not just features. Walk a CAS retry out loud. Senior plus: fail-open vs fail-closed when the distributed counter store is down.

---

## How to Use This in an Interview

1. Pick 2–3 problems that match the prompt (session, streaming, license are high-signal).
2. State **trap → example → fix → one production next step**.
3. Tie to a demo if asked to prove it:  
   `java -cp out com.spotify.lld.demo.MusicStreamingService <auth|session|playlist|streaming|license|limits|...>`

### One-liners interviewers like

1. Player is **per session**, not global.  
2. Nested playlists need **cycle + depth** guards.  
3. Streaming = **chunks → buffer → decoder**.  
4. License = **country ∩ time window**.  
5. Offline = **encrypted + device-bound + expiring**.  
6. Search must be **indexed**.  
7. Recs are driven by **listen events**.  
8. Passwords are **salted hashes**; sessions are **tokens**.  
9. Social = **directed graph + visibility ACL**.  
10. Observer must be **async** with fan-out.  
11. Without **stream + rate limits**, abuse is unbounded.
