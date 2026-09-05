# Chess Game LLD

Low-level design of an online multiplayer chess engine that addresses common interview pitfalls (global locks, O(N) check scans, ArrayList move logs, missing clocks, ignored stalemate).

## Features Required

- **Standard rules:** Piece movement, castling, en passant, promotion; check / checkmate / stalemate.
- **Draws:** Threefold repetition (Zobrist), fifty-move rule, insufficient material.
- **Scale:** Per-game state isolation, Flyweight piece properties, thread-safe moves.
- **Game management:** Create / seat players / terminate; observer notifications; save/load.

## Package structure

```
com.chess.lld
├── piece/      Color, PieceType, PieceProperties, PieceFactory, Piece
├── strategy/   MoveStrategy + Pawn/Rook/Knight/Bishop/Queen/King
├── board/      Position, Board, ZobristHash
├── move/       MoveCommand, CommandHistory
├── game/       Game, GameStatus, PlayerClock
├── player/     GameObserver, Player
├── manager/    GameManager, PlayerManager (singletons)
├── persist/    GameStore, GameSnapshot
└── demo/       ChessGameService + *Scenario demos
```

## Run

```bash
cd ChessGame
javac -d out $(find src -name '*.java')
java -cp out com.chess.lld.demo.ChessGameService          # all
java -cp out com.chess.lld.demo.ChessGameService list     # names
java -cp out com.chess.lld.demo.ChessGameService checkmate
```

Available scenarios: `move`, `concurrency`, `undo`, `checkmate`, `stalemate`, `draw`, `repetition`, `timeout`, `observer`, `flyweight`, `persist`.

## Problems → Solutions

| # | Common mistake | Fix in this codebase |
|---|----------------|----------------------|
| 1 | Race on board / move history | Per-game `ReentrantLock` (single writer) |
| 2 | Full-board scan for check | Attack sets updated after each move; check is O(1) |
| 3 | `ArrayList<Move>` for undo | `Deque` undo + redo stacks |
| 4 | Board compare for repetition | Zobrist hash + `Map<Long,Integer>` |
| 5 | No stalemate / material / clocks | `checkGameState()` + `PlayerClock` + `ScheduledExecutorService` |

## Core flow

```
Player ──join──► PlayerManager ──seat──► Game
Player ──move──► Game.makeMove
                   ├── gameLock.lock
                   ├── validateTurn / validateMove (strategy + self-check)
                   ├── applyMove (Command + attack maps + Zobrist)
                   ├── checkGameState (mate / draw)
                   └── unlock → Observer.update
Turn start → scheduler deadline; move cancels; expiry → TIMEOUT
```

## Patterns used

- **Singleton** — `GameManager`, `PlayerManager`
- **Factory + Flyweight** — `PieceFactory` / `PieceProperties`
- **Strategy** — per-piece `MoveStrategy`
- **Command** — `MoveCommand` + `CommandHistory`
- **Observer** — `GameObserver` / `Player`
- **Single-writer lock** — per `Game`, not a global monitor

## Docs

- `HLD.md` — high-level design
- `README.md` — this file
- `API_REFERENCE.md` — REST-style APIs
- `CLASS_AND_DATA_MODEL.md` — classes + tables
- `PROBLEMS_AND_SOLUTIONS.md` — screenshot pitfalls
- `INTERVIEW_PREP_GUIDE.md` — patterns and traps
- `INTERVIEW_QUESTIONS.md` — senior questions

## Notes

Teaching / interview LLD — not a FIDE-complete engine (no 3-fold claim UI, no increment time-control variants). Clocks, hashes, and attack maps are the points to defend in an interview.
