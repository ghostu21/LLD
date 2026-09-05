# Chess Game LLD — Interview Questions (5+ YOE)

Companions: [`INTERVIEW_PREP_GUIDE.md`](./INTERVIEW_PREP_GUIDE.md) · [`PROBLEMS_AND_SOLUTIONS.md`](./PROBLEMS_AND_SOLUTIONS.md)

---

## 1. Architecture

1. Why is a global lock on `GameManager` the wrong default?
2. How do you shard millions of simultaneous games?
3. What belongs in Redis vs Postgres vs the request thread?
4. Draw the object graph for: two players, one `Game`, one `Board`, flyweight pawns.

## 2. Concurrency

5. Single-writer vs optimistic compare-and-swap on a board version — when?
6. Notify observers inside the lock or after unlock? Deadlock risk?
7. Double-click makeMove from the same player — what happens?

## 3. Rules & algorithms

8. Check vs checkmate vs stalemate in one predicate set.
9. Why is “no legal moves” not the same as “no geometric moves”?
10. Implement O(1) check given attack maps. When are maps stale?
11. Zobrist collisions — do you care for threefold in amateur play?
12. Insufficient material: why piece counts beat scanning the grid every time.

## 4. Persistence & clocks

13. Replay move list vs store FEN after every ply — trade-offs?
14. Why is a browser countdown not a legal clock?
15. Stale scheduled timeout after undo — how do you generation-stamp turns?

## 5. Patterns

16. Flyweight vs interned strings — same idea?
17. Command vs event sourcing for a chess server?
18. Where would you swap Strategy for a Chess960 start position?
