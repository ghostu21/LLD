# Google Drive LLD

Low-level design of a Drive-like file store that addresses common interview pitfalls (role-only ACL, HashMap races, unbounded write locks, no quota, DFS without permission filters).

## Features Required

- **Auth:** Register / login, salted passwords, `ADMIN` vs `USER`.
- **Tree:** Hierarchical files and folders (Composite).
- **Sharing:** Per-node Viewer / Commenter / Editor / Owner; inherit from parents; explicit `NONE` override.
- **Versions:** Immutable snapshots + revert.
- **Search:** DFS by name over nodes the user can view.
- **Quota:** Per-user byte cap; reject writes that would exceed it.

## Package structure

```
com.gdrive.lld
├── account/     User, Role, Group, UserService, PasswordUtils
├── access/      AccessLevel, PermissionsDecorator, AccessDeniedException
├── fs/          FileSystemComponent, DriveNode, File, Folder, FileVersion,
│                FileSystemFactory, FileSystemManager, DriveService
├── proxy/       FileSystemProxy
├── storage/     StorageStrategy, LocalStorageStrategy, CloudStorageStrategy
├── quota/       QuotaService, QuotaExceededException
├── operation/   FileOperation (template), Create/Write/Move, CommandHistory
├── search/      FileSystemSearch
├── events/      DriveObserver, Subject
└── demo/        GoogleDriveService + *Scenario demos
```

## Run

```bash
cd GoogleDrive
javac -d out $(find src -name '*.java')
java -cp out com.gdrive.lld.demo.GoogleDriveService          # all
java -cp out com.gdrive.lld.demo.GoogleDriveService list
java -cp out com.gdrive.lld.demo.GoogleDriveService share
```

Available scenarios: `auth`, `tree`, `share`, `proxy`, `version`, `search`, `quota`, `concurrency`, `move`, `undo`, `observer`, `storage`.

## Problems → Solutions

| # | Common mistake | Fix in this codebase |
|---|----------------|----------------------|
| 1 | Permissions only at user/role level | Per-file/folder `PermissionsDecorator` + inheritance |
| 2 | Non-thread-safe singleton / HashMap | Holder singleton, `ConcurrentHashMap`, COW lists |
| 3 | Unbounded `synchronized` on writes | `ReentrantReadWriteLock.tryLock(timeout)` |
| 4 | Deadlock on move (A→B vs B→A) | Global lock order by name then id |
| 5 | No storage cap | `QuotaService` CAS on `AtomicLong` |

## Core flow

```
User ──login──► UserService
User ──mkdir/create──► FileSystemFactory (owner ACL)
User ──share──► PermissionsDecorator (user or Group)
User ──read/write──► FileSystemProxy.require(level)
                      ├── File.readLock / writeLock.tryLock
                      ├── QuotaService.applyDelta
                      └── StorageStrategy.saveFile
User ──search──► FileSystemSearch DFS (skip unreadable names, still walk children)
User ──move──► ordered locks on source + from + dest
```

## Patterns used

- **Singleton** — `FileSystemManager` (holder idiom)
- **Factory** — `FileSystemFactory`
- **Composite** — `File` / `Folder`
- **Decorator** — `PermissionsDecorator`
- **Proxy** — `FileSystemProxy`
- **Strategy** — `StorageStrategy`
- **Observer** — `Subject` / `DriveObserver`
- **Command + Template Method** — `FileOperation`
- **Read/write lock + lock ordering** — file IO and move

## Docs

- `HLD.md` — high-level design
- `README.md` — this file
- `API_REFERENCE.md` — REST-style APIs
- `CLASS_AND_DATA_MODEL.md` — classes + tables
- `PROBLEMS_AND_SOLUTIONS.md` — screenshot pitfalls
- `INTERVIEW_PREP_GUIDE.md` — patterns and traps
- `INTERVIEW_QUESTIONS.md` — senior questions

## Notes

Teaching / interview LLD — blobs stay in-memory; GCS/S3 and real distributed locks are stubbed.
