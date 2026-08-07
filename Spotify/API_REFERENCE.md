# Spotify LLD — API Reference

REST-style APIs that fulfill the product requirements.  
These are the **HTTP contracts** above this LLD (in-memory services map 1:1 to handlers).

**Auth:** `Authorization: Bearer <token>` unless marked **public**.  
**Base path:** `/v1`

Companions: [`README.md`](./README.md) · [`CLASS_AND_DATA_MODEL.md`](./CLASS_AND_DATA_MODEL.md) · [`PROBLEMS_AND_SOLUTIONS.md`](./PROBLEMS_AND_SOLUTIONS.md)

---

## How to read each API

Every endpoint below includes:

| Section | Meaning |
|---------|---------|
| **What** | Purpose / which requirement it fulfills |
| **Working logic** | Step-by-step server flow |
| **Request / Response** | Contract |
| **Useful info** | Auth, errors, idempotency, LLD mapping, interview tips |

---

## Requirement → API Map

| # | Requirement | APIs |
|---|-------------|------|
| 1 | Auth & registration | register, login, logout, me |
| 2 | Catalog browse/search | search, get song |
| 3 | Streaming / playback | sessions + play/pause/resume/stop |
| 4 | Recommendations | listen-events, recommendations |
| 5 | Playlists | create, get, add/remove items, patch |
| 6 | Social | follow, unfollow, visible playlists |
| 7 | Offline | download, list, play offline |
| 8 | Licensing | gate on play + admin register + availability |

---

## Common conventions

### Error body
```json
{
  "error": {
    "code": "LICENSE_DENIED",
    "message": "Track not available in IN",
    "details": { "songId": "...", "country": "IN" }
  }
}
```

| HTTP | When |
|------|------|
| 400 | Validation |
| 401 | Missing/invalid/expired token |
| 403 | License / ACL / stream limit |
| 404 | Not found |
| 409 | Conflict (playlist cycle) |
| 429 | Rate limited |

### Useful globals
- **Idempotency:** mutating APIs may accept `Idempotency-Key` header in production.
- **Rate limit:** per-user token bucket (`RateLimiter`) → `429`.
- **Country:** taken from `User.countryCode` for license checks (not client-spoofed in production; derive from account / payment geo carefully).

---

## 1. Authentication & Registration

### `POST /v1/auth/register` (public)

**What**  
Creates a new user account. Fulfills **User Authentication and Registration**. Never stores plaintext passwords.

**Working logic**
1. Validate username unique, password strength, `countryCode` format.
2. Generate random **salt**.
3. Compute `passwordHash = hash(password, salt)` (`PasswordUtils`).
4. Persist `User` (userId, username, hash, salt, country).
5. Return public profile (never return hash/salt).

**Request**
```json
{
  "username": "alice",
  "password": "secret123",
  "countryCode": "US"
}
```

**Response `201`**
```json
{
  "userId": "u-111",
  "username": "alice",
  "countryCode": "US"
}
```

**Useful info**
- Maps to: `AuthService.register`, `User`, `PasswordUtils`
- Does **not** auto-login — call `login` next (or issue token in same response as product choice).
- Interview: salt defeats rainbow tables; production uses Argon2id/BCrypt, not raw SHA-256.

---

### `POST /v1/auth/login` (public)

**What**  
Verifies credentials and issues a short-lived **session token** for subsequent APIs.

**Working logic**
1. Lookup user by username.
2. Re-hash candidate password with stored salt; compare to `passwordHash`.
3. On mismatch → `401` (same message for unknown user to avoid user enumeration).
4. Create `AuthToken` (UUID, userId, expiresAt = now+1h).
5. Store token; return token to client.

**Request**
```json
{
  "username": "alice",
  "password": "secret123"
}
```

**Response `200`**
```json
{
  "token": "a1b2c3d4-...",
  "userId": "u-111",
  "expiresAt": "2026-08-06T18:00:00Z"
}
```

**Useful info**
- Maps to: `AuthService.login` → `AuthToken`
- Client sends `Authorization: Bearer <token>` afterward.
- Production: JWT access + refresh token rotation; store refresh server-side for revoke.

---

### `POST /v1/auth/logout`

**What**  
Revokes the current session token immediately.

**Working logic**
1. Authenticate bearer token.
2. Delete token from store (`AuthService.logout`).
3. Return `204`. Subsequent calls with that token → `401`.

**Request:** empty body  

**Response `204`** No Content

**Useful info**
- Logout is **server-side revoke**, not only client clearing storage.
- Multi-device: revoke one token or all user tokens (`logout-all` extension).

---

### `GET /v1/me`

**What**  
Returns the authenticated user’s profile (post-login “who am I”).

**Working logic**
1. Validate token → resolve `userId`.
2. Load `User`; return safe fields + lightweight stats (e.g. playlist count).

**Response `200`**
```json
{
  "userId": "u-111",
  "username": "alice",
  "countryCode": "US",
  "playlistCount": 3
}
```

**Useful info**
- Use for app bootstrap after cold start with stored token.
- Never include password hash/salt.

---

## 2. Music Catalog & Search

### `GET /v1/catalog/search` (public)

**What**  
Search songs by partial title or artist. Fulfills **Music Catalog** browse/search. Guests allowed.

**Working logic**
1. Normalize query (`q`) to lowercase.
2. Choose index: `by=title` → title Trie; `by=artist` → artist Trie.
3. Walk Trie path → collect matching `songId`s (~O(k) on query length).
4. Hydrate `Song` objects from catalog map; apply `limit`.
5. Return results (no audio bytes).

**Query:** `q=bohe&by=title&limit=20`

**Response `200`**
```json
{
  "query": "bohe",
  "by": "title",
  "results": [
    {
      "songId": "s-1",
      "title": "Bohemian Rhapsody",
      "artist": "Queen",
      "album": "A Night at the Opera",
      "genre": "Rock"
    }
  ]
}
```

**Useful info**
- Maps to: `MusicCatalog` + `CatalogSearchIndex` (suffix Trie)
- Interview trap: do **not** scan `List<Song>` — dies at catalog scale.
- Production: Elasticsearch/OpenSearch; Trie is LLD teaching model.
- Empty `results: []` is success (`200`), not `404`.

---

### `GET /v1/songs/{songId}` (public)

**What**  
Fetch one track’s metadata for detail page / play prep.

**Working logic**
1. Lookup song by id in catalog.
2. Missing → `404`.
3. Return metadata only (stream URL obtained via play/session APIs).

**Response `200`**
```json
{
  "songId": "s-1",
  "title": "Bohemian Rhapsody",
  "artist": "Queen",
  "album": "A Night at the Opera",
  "genre": "Rock",
  "durationMs": 354000
}
```

**Useful info**
- Pair with `/availability?country=` before showing Play in geo-blocked regions.

---

## 3. Music Streaming & Playback

### `POST /v1/sessions`

**What**  
Opens a **per-device playback session**. Fulfills streaming requirement without a global `MusicPlayer` singleton.

**Working logic**
1. Authenticate user.
2. `SessionManager.createSession(user)` → new `PlaybackSession` with its own `MusicPlayer`.
3. Bind optional `deviceId` for multi-device / stream-limit accounting.
4. Return `sessionId` + initial `STOPPED` state.

**Request**
```json
{ "deviceId": "iphone-14-alice" }
```

**Response `201`**
```json
{
  "sessionId": "sess-99",
  "userId": "u-111",
  "state": "STOPPED"
}
```

**Useful info**
- Interview: User A pause must not stop User B → session isolation.
- One user can have multiple sessions (phone + laptop) until stream cap blocks.

---

### `POST /v1/sessions/{sessionId}/play`

**What**  
Starts playback/streaming of a song on that session. Core of **Music Streaming**.

**Working logic**
1. Auth + load session; ensure session belongs to caller.
2. `RateLimiter.tryConsume(userId)` → else `429`.
3. `LicenseService.assertPlayable(songId, user.country)` → else `403 LICENSE_DENIED`.
4. `StreamLimiter.tryAcquireStream(userId)` → else `403 STREAM_LIMIT`.
5. `PlaybackSession.play(song)` → build `AudioStreamBuffer` → `StreamingPlayer` consumes chunks → decoder.
6. Emit `ListenEvent(PLAY)` for recommendations (async).
7. Return PLAYING + stream hints.

**Request**
```json
{ "songId": "s-1" }
```

**Response `200`**
```json
{
  "sessionId": "sess-99",
  "state": "PLAYING",
  "songId": "s-1",
  "stream": {
    "chunkSize": 4096,
    "protocol": "chunked"
  }
}
```

**Useful info**
- Maps to: License → Limits → Session → Streaming pipeline
- Order of checks matters: don’t acquire stream slot before license fails.
- Production: return signed CDN URL / HLS playlist instead of raw chunks.
- `song.play()` alone is **not** streaming — chunks + buffer + decoder is.

---

### `POST /v1/sessions/{sessionId}/pause` | `/resume` | `/stop`

**What**  
Transport controls for the session’s player state machine.

**Working logic**
| API | Logic |
|-----|--------|
| pause | If PLAYING → PAUSED (buffer may keep warm) |
| resume | If PAUSED → PLAYING |
| stop | Stop streaming worker, clear current track, **release stream slot** |

**Response example (`stop`)**
```json
{ "sessionId": "sess-99", "state": "STOPPED" }
```

**Useful info**
- Always release `StreamLimiter` on stop/end — else user is stuck at cap.
- Pause typically keeps the slot held (product choice); free tier may release on pause.

---

### `GET /v1/sessions/{sessionId}/stream` (optional)

**What**  
Chunked audio bytes for the active play (demo of producer–consumer).

**Working logic**  
Client pulls / server pushes chunks from `AudioStreamBuffer` while `StreamingPlayer` decodes. Backpressure via consumer pace.

**Useful info**  
Production apps use HLS/DASH; this endpoint explains LLD streaming architecture in interviews.

---

## 4. Personalized Recommendations

### `POST /v1/listen-events`

**What**  
Records a user–track interaction that drives recommendations. Fulfills **Personalized Recommendations** input side.

**Working logic**
1. Auth user.
2. Validate `type` + `songId` + optional duration.
3. Build `ListenEvent`; `RecommendationEngine.onEvent` applies score delta:
   - PLAY +1, LIKE +3, REPEAT +4, SHARE +5
   - SKIP −2 if &lt;10s else −0.5
4. Persist/append event (Kafka in prod); return `202 Accepted`.

**Request**
```json
{
  "songId": "s-1",
  "type": "LIKE",
  "listenDurationMs": 0
}
```

**Response `202`**
```json
{ "accepted": true, "eventId": "le-555" }
```

**Useful info**
- Recommendations are **event-driven**, not a magic `getRecommendations()` stub.
- Often auto-fired by play/skip in the player client.

---

### `GET /v1/recommendations`

**What**  
Returns top-N personalized track ids for the user.

**Working logic**
1. Auth → `userId`.
2. `RecommendationEngine.recommend(userId, limit)` sorts affinity desc.
3. Hydrate track metadata for UI.
4. Cold start (no events): fall back to genre/popularity (stub in LLD).

**Query:** `limit=10`

**Response `200`**
```json
{
  "userId": "u-111",
  "trackIds": ["s-1", "s-2"],
  "tracks": [
    { "songId": "s-1", "title": "Bohemian Rhapsody", "artist": "Queen" }
  ]
}
```

**Useful info**
- Production: collaborative filtering / embeddings offline; API reads a ranked cache.

---

## 5. Playlist Management

### `POST /v1/playlists`

**What**  
Creates a playlist owned by the caller. Fulfills **Playlist Management**.

**Working logic**
1. Auth → ownerId.
2. Create `Playlist(name, ownerId, visibility)` default PRIVATE.
3. Attach to user’s playlist list; return resource.

**Request**
```json
{
  "name": "Road Trip",
  "visibility": "PUBLIC"
}
```

**Response `201`** — playlist with empty `items`

**Useful info**  
Visibility drives social ACL later (`PUBLIC` / `FOLLOWERS_ONLY` / `PRIVATE`).

---

### `GET /v1/playlists/{playlistId}`

**What**  
Reads a playlist if ACL allows the viewer.

**Working logic**
1. Load playlist.
2. `PlaylistAccessControl.canView(viewerId, playlist)` using `SocialGraph` for FOLLOWERS_ONLY.
3. Deny → `403`; else return items (songs + nested playlists).

**Useful info**
- Interview: “Can Bob see Alice’s playlist?” = visibility + follow edge.

---

### `POST /v1/playlists/{playlistId}/items`

**What**  
Adds a song or nested playlist with **cycle/depth guards** (Composite safety).

**Working logic**
1. Auth; ensure caller is owner (or collaborator — extension).
2. If adding playlist: `wouldCreateCycle` DFS → else `409 PLAYLIST_CYCLE`.
3. Reject self-add.
4. Append to COW list; return updated playlist.

**Request**
```json
{ "type": "PLAYLIST", "playlistId": "pl-2" }
```

**Response `409`**
```json
{
  "error": {
    "code": "PLAYLIST_CYCLE",
    "message": "Adding this playlist would create a circular reference."
  }
}
```

**Useful info**
- Maps to: `Playlist.addSong` + `MAX_NESTING_DEPTH` on play.
- Concurrent edit/play uses `CopyOnWriteArrayList` in LLD.

---

### `DELETE /v1/playlists/{playlistId}/items/{itemId}` · `PATCH /v1/playlists/{playlistId}`

**What**  
Remove an item or update name/visibility.

**Working logic**  
Owner-only mutate; patch visibility immediately affects ACL for followers.

---

## 6. Social Features

### `POST /v1/users/{userId}/follow` · `DELETE .../follow`

**What**  
Creates/removes a **directed** follow edge. Fulfills **Social Features**.

**Working logic**
1. Auth as follower.
2. Reject self-follow.
3. `SocialGraph.follow(follower, followee)` updates both adjacency maps.
4. Publish `USER_FOLLOWED` event → async notification.
5. Unfollow removes both sides of the edge.

**Response `200`**
```json
{ "followerId": "u-222", "followeeId": "u-111", "following": true }
```

**Useful info**
- Follow is **not** symmetric — needed for `FOLLOWERS_ONLY` playlists.
- Idempotent follow is a good production default.

---

### `GET /v1/users/{userId}/playlists`

**What**  
Lists that user’s playlists **filtered by ACL** for the caller.

**Working logic**
1. Load owner’s playlists.
2. Filter with `PlaylistAccessControl.getVisiblePlaylists(viewer, owner, list)`.
3. Return only PUBLIC / allowed FOLLOWERS_ONLY / own PRIVATE.

**Useful info**  
Never return PRIVATE playlists to non-owners — privacy/trust risk.

---

## 7. Offline Mode

### `POST /v1/offline/downloads`

**What**  
Downloads a track for offline use with DRM-style constraints. Fulfills **Offline Mode**.

**Working logic**
1. Auth; check license still allows user country.
2. Enforce offline quota (`DownloadManager` max per user).
3. Encrypt audio bytes; store path.
4. Create `OfflineTrack` with **deviceId**, **expiresAt (~30 days)**, **licenseToken**.
5. Return download metadata (not plaintext file URL).

**Request**
```json
{ "songId": "s-1", "deviceId": "iphone-14-alice" }
```

**Response `201`**
```json
{
  "downloadId": "dl-1",
  "songId": "s-1",
  "deviceId": "iphone-14-alice",
  "expiresAt": "2026-09-05T12:00:00Z",
  "encrypted": true
}
```

**Useful info**
- Interview: offline ≠ “save MP3”. Must be device-bound + time-bound + encrypted.
- Production: Widevine/FairPlay + KMS keys.

---

### `GET /v1/offline/downloads` · `POST /v1/offline/play`

**What**  
List downloads for a device; attempt offline play with gates.

**Working logic (play)**
1. Find download by user+song.
2. `OfflineTrack.isPlayable(deviceId)` → device match AND not expired AND token present.
3. Fail → `403`; success → return decryption handle / local stream.

**Useful info**  
Wrong device or expired license must fail closed.

---

## 8. Licensing

### Enforced on `POST .../play` (see §3)

**What**  
Legal geo/time gate before any stream starts.

**Working logic**  
`canPlay = country ∈ allowedCountries AND now ∈ (validFrom, validUntil)`.

---

### `POST /v1/admin/licenses` (admin)

**What**  
Registers/updates a track’s license window and countries.

**Working logic**  
Upsert `License` in `LicenseService`; used by all future play checks.

**Request**
```json
{
  "songId": "s-1",
  "allowedCountries": ["US", "GB"],
  "validFrom": "2020-01-01T00:00:00Z",
  "validUntil": "2030-12-31T23:59:59Z"
}
```

---

### `GET /v1/songs/{songId}/availability`

**What**  
UI helper: “Can I show the Play button in this country?”

**Query:** `country=IN`  

**Response**
```json
{ "songId": "s-1", "country": "IN", "playable": false }
```

**Useful info**  
Always re-check on play — availability can change between page load and tap.

---

## Notifications (async)

### `GET /v1/notifications` (optional inbox)

**What**  
Fetches in-app notifications produced by social/order-like music events.

**Working logic**  
`AsyncEventBus.publish` → workers → `NotificationService` stores/pushes messages. Inbox API reads user’s feed.

**Useful info**
- Observer must be **async** — never block play path on email/push.
- Production: Kafka/SNS + push providers (FCM/APNs).

---

## End-to-end “Press Play” sequence

```
login → create session → play(songId)
  → rate limit
  → license check (country + window)
  → acquire stream slot (CAS)
  → chunked stream
  → emit listen event
  → (async) update recommendations / notify friends
```

## Interview closer

> “Each API is a thin adapter; invariants live in domain services — session isolation, license before stream, versioned/guarded playlists, offline DRM constraints, and async fan-out for notifications.”
