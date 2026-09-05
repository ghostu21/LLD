# Chess Game LLD — Problems Faced & Solutions

Each item is a common LLD interview pitfall from the design screenshots.

Companions: [`INTERVIEW_PREP_GUIDE.md`](./INTERVIEW_PREP_GUIDE.md) · [`INTERVIEW_QUESTIONS.md`](./INTERVIEW_QUESTIONS.md)

---

## Quick Map

| # | Problem | Fix |
|---|---------|-----|
| 1 | Concurrent moves / race on game state | Per-game `ReentrantLock` |
| 2 | Inefficient move validation / check | Piece strategies + attack maps |
| 3 | ArrayList move log | Undo/redo `Deque` + Zobrist index |
| 4 | No mate / stalemate / material | `checkGameState()` |
| 5 | Game stalls forever | `PlayerClock` + scheduler |

---

## 1. Concurrency in online multiplayer

### Problem
Two players (or double-submit) mutate board and history at once. No lock → torn state.

### Example
```
T1: validate e2-e4 empty
T2: validate d2-d4 empty
Both apply → two white moves, turn corrupted
```

### Solution
Each `Game` owns `gameLock`. `makeMove` is lock → validateTurn → validateMove → applyMove → checkGameState → unlock.

**Not** `synchronized` on `GameManager`: too coarse, kills throughput, hard to reason about.

**Code:** `game/Game.java`  
**Demo:** `concurrency`

### Interviewer perspective
They want **single writer per aggregate**, same idea as per-vehicle locks in rental LLD. Mention actor-per-game or Redis single-thread Lua as prod cousins.

---

## 2. Inefficient move validation / check detection

### Problem
Scanning the whole board every ply for “is the king attacked?” is O(board) and gets worse with naive nested loops.

### Solution
- **Piece-centric:** `MoveStrategy` per type (already the right abstraction).
- **Attack maps:** `Set` of squares attacked by White and Black, rebuilt after each move.
- **Fast king safety:** `opponentAttackSet.contains(kingPosition)` → O(1).

Interview line: *Never recompute what can be incrementally updated.*

**Code:** `board/Board.java`  
**Demo:** `checkmate`, `move`

---

## 3. Inefficient game log storage

### Problem
`ArrayList<Move>` is a poor undo/redo structure (and a poor search index).

### Solution
- `Deque undoStack` / `Deque redoStack` — O(1) undo/redo.
- Zobrist `boardHash` + `Map repetitionCount` — threefold in O(1), no board compare.

Interview line: *Game logs are for humans. Hashes are for engines.*

**Code:** `move/CommandHistory.java`, `board/ZobristHash.java`  
**Demo:** `undo`, `repetition`

---

## 4. No stalemate & checkmate (edge cases ignored)

### Problem
Only validating a single move, never the **future** of the position.

### Solution
After each ply:
```
inCheck && noLegalMoves  → CHECKMATE
!inCheck && noLegalMoves → STALEMATE
K vs K / K+B vs K / K+N vs K → DRAW_INSUFFICIENT
```

Interview line: *Chess legality is about the future, not just the move.*

**Code:** `Game.checkGameState`  
**Demo:** `checkmate`, `stalemate`, `draw`

---

## 5. No timeout — game can stall

### Problem
If a player never moves, the match hangs forever.

### Solution
`PlayerClock.remainingTimeMillis` + `ScheduledExecutorService`:
- Turn start → timer scheduled  
- Move received → timer cancelled  
- Expiry → `TIMEOUT`, opponent wins  

Interview line: *Timeouts are part of game rules, not UI features.*

**Code:** `game/PlayerClock.java`, `Game.armDeadline`  
**Demo:** `timeout`
