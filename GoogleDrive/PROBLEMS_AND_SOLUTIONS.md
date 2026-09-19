# Google Drive LLD — Problems Faced & Solutions

Each item maps to the design screenshots: **problem**, **example**, **fix**, **interviewer view**.

Companions: [`INTERVIEW_PREP_GUIDE.md`](./INTERVIEW_PREP_GUIDE.md) · [`INTERVIEW_QUESTIONS.md`](./INTERVIEW_QUESTIONS.md)

---

## Quick Map

| # | Problem | Fix |
|---|---------|-----|
| 1 | ACL only global / missing file-level | Per-node decorator + inherit + `NONE` |
| 2 | Singleton / collections not thread-safe | Holder + CHM + COW |
| 3 | Race on file content | `ReentrantReadWriteLock` |
| 4 | Deadlock / infinite wait | Lock order + `tryLock` timeout |
| 5 | No quota | Atomic usage + CAS |

---

## 1. File-based permissions (missing feature)

### Problem
Role on `User` is not enough. Drive shares **this file** as Viewer and **that folder** as Editor. Inheritance must walk parents; an explicit `NONE` must hide a file inside a shared folder.

### Example
```
Reports/  VIEWER for group Viewers
  SalesReport.pdf  NONE for user viewer
Viewer can list Reports, cannot open SalesReport
```

### Solution
`PermissionsDecorator` on every node. `FileSystemProxy.getEffectiveAccessLevel` returns the **first** direct user/group grant walking parentward.

**Code:** `access/PermissionsDecorator.java`, `proxy/FileSystemProxy.java`  
**Demo:** `share`, `proxy`

### Interviewer perspective
They want “closest explicit ACE wins”, not max(parent, child). Mention Google’s “Can’t share” vs inherited reader.

---

## 2. Multi-thread: singleton and collections

### Problem
Lazy `if (instance == null) instance = new …` and `HashMap` children blow up under concurrent create.

### Solution
Initialization-on-demand holder for `FileSystemManager`. `ConcurrentHashMap` for children and ACL. `CopyOnWriteArrayList` for observers and versions.

**Demo:** `tree`, `observer`

---

## 3. File IO races

### Problem
Two editors write the same doc → torn content, duplicate version ids.

### Solution
`ReentrantReadWriteLock`: many readers, one writer. `AtomicLong` version ids. Immutable `FileVersion`.

**Demo:** `version`, `concurrency`

---

## 4. Deadlocks and unbounded waits

### Problem
Move A into B while someone moves B into A. Writer waits forever on a crashed holder.

### Solution
Acquire `{source, from, dest}` sorted by `name` then `id`. `tryLock(5s)` on file writes and on move.

**Demo:** `move`, `concurrency`

---

## 5. Quota

### Problem
Unlimited writes → noisy neighbor.

### Solution
`QuotaService.applyDelta` CAS loop; throw `QuotaExceededException` before mutating the file.

**Demo:** `quota`
