# Spotify LLD

Low-level design of a music streaming service that addresses the common interview pitfalls (thread-unsafe singleton player, fake streaming, missing licenses, plaintext auth, etc.).

## Features Required

- **User Authentication and Registration:** Users should be able to create accounts, log in, and manage their profiles.
- **Music Catalog:** The service should have a catalog of songs, albums, artists, and playlists that users can browse and search.
- **Music Streaming:** Users should be able to stream music and listen to songs, albums, playlists, and personalized recommendations.
- **Personalized Recommendations:** The service should provide personalized recommendations based on user preferences, listening history, and user interactions.
- **Playlist Management:** Users should be able to create, modify, and manage their own playlists.
- **Social Features:** Users should be able to follow other users, view their public playlists, and share music with friends.
- **Offline Mode:** Users should have the option to download songs and listen to them offline.
- **Music Licensing:** The service should comply with music licensing regulations and obtain necessary licenses to stream music legally.

## Package structure

```
com.spotify.lld
├── auth/            User, PasswordUtils, AuthToken, AuthService
├── playback/        SessionManager, PlaybackSession, MusicPlayer, PlayerState
├── streaming/       StreamBuffer, AudioStreamBuffer, StreamingPlayer, AudioDecoder
├── catalog/         Music, Song, MusicCatalog, CatalogSearchIndex, TrieNode
├── playlist/        Playlist, PlaylistVisibility
├── license/         License, LicenseService, LicenseException
├── offline/         DownloadManager, OfflineTrack
├── social/          SocialGraph, PlaylistAccessControl
├── events/          AsyncEventBus, MusicEvent, MusicEventListener, NotificationService
├── recommendation/  ListenEvent, ListenEventType, RecommendationEngine
├── limits/          StreamLimiter, RateLimiter, TokenBucket
└── demo/            MusicStreamingService + *Scenario demos
```

## Run

```bash
javac -d out $(find src -name '*.java')
java -cp out com.spotify.lld.demo.MusicStreamingService          # all
java -cp out com.spotify.lld.demo.MusicStreamingService list     # names
java -cp out com.spotify.lld.demo.MusicStreamingService auth     # one
```

Available scenarios: `auth`, `session`, `playlist`, `streaming`, `catalog`, `license`, `recommend`, `social`, `events`, `offline`, `limits`.

## Problems → Solutions

| # | Common mistake | Fix in this codebase |
|---|----------------|----------------------|
| 1 | Lazy singleton `MusicPlayer` not thread-safe | Per-session player — no shared global instance |
| 2 | One global player for all users | `SessionManager` → `PlaybackSession` → `MusicPlayer` |
| 3 | `ArrayList` playlist under concurrent edit/play | `CopyOnWriteArrayList` |
| 4 | Composite playlist cycles → `StackOverflowError` | Cycle detection + max nesting depth on add/play |
| 5 | `song.play()` ≠ streaming | `Track → AudioStreamBuffer → StreamingPlayer → Decoder` |
| 6 | Offline = “save file” | Device-bound, 30-day expiry, encrypted path + license token |
| 7 | No geo/time licenses | `License` + `LicenseService.assertPlayable(track, country)` |
| 8 | Recommendations as a method call | `ListenEvent` stream → affinity scores / ranking |
| 9 | Scan `List<Song>` for search | Suffix-Trie `CatalogSearchIndex` (prefix / partial match) |
| 10 | Plaintext password | Salt + SHA-256 hash + expiring `AuthToken` |
| 11 | Follow/share with no graph/ACL | `SocialGraph` + `PlaylistVisibility` + `PlaylistAccessControl` |
| 12 | Claimed Observer, no events | Async `AsyncEventBus` + `NotificationService` |
| 13 | No stream/API limits | CAS `StreamLimiter` + token-bucket `RateLimiter` |

## Core flow

```
User ──login──► AuthService ──token──► SessionManager
                                           │
                                           ▼
                                    PlaybackSession
                                           │
                    LicenseService ◄── assert ──┤
                                           │
                                           ▼
                                      MusicPlayer
                                           │
                              StreamingPlayer + StreamBuffer
```

Social / notify path: listen or follow actions → `AsyncEventBus` → listeners (notifications, recommenders).

## Patterns used

- **Per-session state** instead of a platform-wide player singleton  
- **Composite** for nested playlists (with cycle guards)  
- **Observer** via async event bus  
- **Trie** for catalog indexing  
- **Token bucket** + **CAS** for abuse protection  

## Docs

- `README.md` — this file (structure + run)
- `API_REFERENCE.md` — REST APIs with request/response per requirement
- `CLASS_AND_DATA_MODEL.md` — class relationships + DB tables
- `PROBLEMS_AND_SOLUTIONS.md` — 13 interview pitfalls with examples + interviewer perspective
- `INTERVIEW_PREP_GUIDE.md` — patterns, concepts, trap answers for interviews
- `INTERVIEW_QUESTIONS.md` — senior (5+ YOE) interview questions by topic

## Notes

This is an LLD teaching / interview codebase — DRM encryption, real audio codecs, and production auth (BCrypt/Argon2, JWT) are stubbed where noted in comments.
