# Google Drive LLD — Interview Questions (5+ YOE)

Companions: [`INTERVIEW_PREP_GUIDE.md`](./INTERVIEW_PREP_GUIDE.md) · [`PROBLEMS_AND_SOLUTIONS.md`](./PROBLEMS_AND_SOLUTIONS.md)

---

## 1. Architecture

1. Why split metadata (tree + ACL) from blob bytes?
2. What is eventually consistent after a write — search index, notifications, storage replica?
3. Draw the object graph for: Alice owns Reports/, Bob is Viewer on the folder, Carol is NONE on one PDF.

## 2. ACL

4. Inheritance vs explicit ACE — which wins and why must `NONE` be stored?
5. User ACE vs group ACE on the same node?
6. ADMIN role bypass — is that a product feature or a support hatch?
7. How do you audit “who could read this file last Tuesday?”

## 3. Concurrency

8. Holder idiom vs double-checked locking — when do you still need `volatile`?
9. Why RW lock instead of `synchronized (file)` for reads?
10. Specify lock order for move. What if two files share a name in different folders?
11. `tryLock(5s)` — what does the client retry?

## 4. Data & quota

12. CAS on used-bytes vs `SELECT … FOR UPDATE` in the DB.
13. Version as new object vs copy-on-write chunks for 2 GB files.
14. DFS search vs inverted index — what breaks at 10M files?

## 5. Patterns

15. Decorator vs a column on `Node` — when is the pattern overhead silly?
16. Command undo vs an operation log (event source) for Drive?
17. Strategy for storage — how do you dual-write during a GCS migration?
