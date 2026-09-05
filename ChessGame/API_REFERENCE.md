# Chess Game LLD — API Reference

REST-style APIs for requirement fulfillment.

Auth: `Authorization: Bearer <token>`  
Base: `/v1`

Companions: [`README.md`](./README.md) · [`CLASS_AND_DATA_MODEL.md`](./CLASS_AND_DATA_MODEL.md)

---

## Requirement → API Map

| Requirement | APIs |
|-------------|------|
| Create / get game | `POST /games`, `GET /games/{id}` |
| Seat players | `POST /games/{id}/join` |
| Move / undo | `POST /games/{id}/moves`, `POST /games/{id}/undo` |
| Resign | `POST /games/{id}/resign` |
| Snapshot | `GET /games/{id}/snapshot`, `POST /games/{id}/restore` |

### Errors
| HTTP | When |
|------|------|
| 409 | Not your turn / illegal move / game over |
| 404 | Unknown game |
| 408 | Lock not acquired (should be rare; per-game lock is short) |

---

## `POST /v1/games`

**What**  
Create a match with optional per-move clock.

**Working logic**  
`GameManager.createGame(moveTimeMillis)` — no global lock.

**Request**
```json
{ "moveTimeMillis": 300000 }
```

**Response**
```json
{ "gameId": "uuid", "status": "IN_PROGRESS", "turn": "WHITE" }
```

---

## `POST /v1/games/{id}/join`

**What**  
Seat the caller as White (first) or Black (second).

**Working logic**  
`PlayerManager.assignPlayerToGame`; registers `GameObserver`.

**Request**
```json
{ "playerId": "alice" }
```

**Response**
```json
{ "playerId": "alice", "color": "WHITE" }
```

---

## `POST /v1/games/{id}/moves`

**What**  
Play one ply. Coordinates: `x` file 0–7 (a–h), `y` rank 0–7 (1–8).

**Working logic**  
`gameLock` → validate turn → strategy + self-check → apply command → attack maps / Zobrist → `checkGameState` → unlock → notify.

**Request**
```json
{ "playerId": "alice", "startX": 4, "startY": 1, "endX": 4, "endY": 3 }
```

**Response**
```json
{ "ok": true, "status": "IN_PROGRESS", "turn": "BLACK", "check": false }
```

**Useful info**  
Illegal move returns `ok: false` and 409; clock cancelled on success.

---

## `POST /v1/games/{id}/undo`

**What**  
Pop undo stack, push redo (O(1)).

**Response**
```json
{ "ok": true, "turn": "WHITE" }
```

---

## `GET /v1/games/{id}`

**What**  
Board ASCII / FEN-equivalent plus status, clocks, last move.

**Response**
```json
{
  "status": "CHECKMATE",
  "winner": "BLACK",
  "turn": "WHITE",
  "whiteRemainingMillis": 280000,
  "blackRemainingMillis": 295000
}
```

---

## `POST /v1/games/{id}/resign`

**What**  
Opponent wins; cancel deadline.

---

## `GET /v1/games/{id}/snapshot` / `POST .../restore`

**What**  
Persist move list (`GameStore`) and replay into a new `Game`.
