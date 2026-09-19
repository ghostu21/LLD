# Google Drive LLD — Interview Prep Guide

---

## 1. What Are We Building?

A **Drive-like** tree: auth, hierarchical files/folders, sharing, versions, search, quota.

**One-liner:**  
> “Composite tree, per-node ACL with inheritance and NONE override, Proxy for checks, RW-lock + tryLock on blobs, ordered locks on move, Holder singleton, Strategy for storage, Template/Command for undo, AtomicLong quota.”

---

## 2. Patterns

| Pattern | Where | Why |
|---------|-------|-----|
| Singleton | FileSystemManager | One tree |
| Factory | FileSystemFactory | Owner ACE on create |
| Composite | File / Folder | Uniform tree ops |
| Decorator | PermissionsDecorator | ACL without subclassing File |
| Proxy | FileSystemProxy | Enforce ACE |
| Strategy | StorageStrategy | Local vs cloud |
| Observer | Subject | Share / content notify |
| Template + Command | FileOperation | validate/perform/log + undo |

---

## 3. Trap answers

| Trap | Better |
|------|--------|
| `synchronized` the whole manager | Per-file RW lock |
| Max(parent, child) ACL | Closest explicit ACE |
| `HashMap` children | `ConcurrentHashMap` |
| Wait forever on write | `tryLock` timeout |
| DFS as prod search | Index; DFS is the LLD algorithm |

---

## 4. Permission ladder

`OWNER > EDITOR > COMMENTER > VIEWER > NONE`  
Lower ordinal = more power.
