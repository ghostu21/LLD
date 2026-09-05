# Chess Game LLD — Interview Prep Guide

Read before the interview: **what to build**, **patterns**, **concepts**, **traps**.

---

## 1. What Are We Building?

An **online chess match service**:
standard rules, many concurrent games, legal-move generation, clocks.

**One-liner:**  
> “Per-game ReentrantLock (single writer), Strategy for piece moves, Command+Deque undo/redo, Flyweight properties, Zobrist for threefold O(1), attack sets for O(1) check, and a server-side move clock.”

---

## 2. Patterns

| Pattern | Where | Why |
|---------|-------|-----|
| Singleton | GameManager, PlayerManager | One registry, not one board |
| Factory | PieceFactory | Wire type → strategy |
| Flyweight | PieceProperties | Share (PAWN,WHITE) across games |
| Strategy | MoveStrategy | Piece rules without subclass explosion |
| Command | MoveCommand | Undo/redo / audit |
| Observer | GameObserver | Notify without coupling to sockets |

---

## 3. Core Concepts

- **Single writer:** lock lives on `Game`, not the JVM.
- **Attack map vs move gen:** attacks include protected squares; legal moves also forbid self-check.
- **Zobrist:** XOR keys for piece-on-square, side to move, castling, en passant file.
- **Derived state:** attack sets and hashes are not source of truth — the grid is; they must update with every mutation.

---

## 4. Trap answers

| Trap | Better |
|------|--------|
| `synchronized` on GameManager | Per-game lock / actor |
| Scan 64 squares to test check | King in opponent attack set |
| List of full board clones for repetition | Zobrist map |
| Client JS timer | Server scheduler |
| Piece subclasses only | Strategy + flyweight properties |

---

## 5. Walk the board

Coordinates: `x` = file a=0 … h=7, `y` = rank 1=0 … 8=7.  
`makeMove(4,1,4,3)` is e2-e4.
