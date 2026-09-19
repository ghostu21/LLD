# Google Drive LLD — API Reference

Auth: `Authorization: Bearer <token>`  
Base: `/v1`

Companions: [`README.md`](./README.md) · [`CLASS_AND_DATA_MODEL.md`](./CLASS_AND_DATA_MODEL.md)

---

## Requirement → API Map

| Requirement | APIs |
|-------------|------|
| Auth | `POST /auth/register`, `POST /auth/login` |
| Tree | `POST /folders`, `POST /files`, `GET /nodes/{id}` |
| Share | `POST /nodes/{id}/permissions` |
| Write / version | `PUT /files/{id}/content`, `POST /files/{id}/revert` |
| Search | `GET /search?q=` |
| Move | `POST /nodes/{id}/move` |
| Quota | `GET /users/me/quota` |

### Errors
| HTTP | When |
|------|------|
| 401 | Bad credentials |
| 403 | ACL deny (`AccessDeniedException`) |
| 409 | Name clash / validation failed |
| 413 | Quota exceeded |
| 408 | Lock timeout |

---

## `POST /v1/auth/register`

**Request** `{ "username": "alice", "password": "secret" }`  
**Response** `{ "username": "alice", "role": "USER" }`

---

## `POST /v1/files`

**Working logic**  
`CreateFileOperation` → factory sets `OWNER` on caller → parent `add`.

**Request** `{ "parentId": "...", "name": "notes.txt" }`

---

## `PUT /v1/files/{id}/content`

**Working logic**  
Proxy `EDITOR` → quota delta → `writeLock.tryLock(5s)` → append immutable `FileVersion` → `StorageStrategy.saveFile`.

**Request** `{ "content": "hello" }`  
**Response** `{ "versionId": 2, "size": 5 }`

---

## `POST /v1/nodes/{id}/permissions`

**Working logic**  
Requires `OWNER`. User grant beats group; `NONE` is stored and blocks inheritance.

**Request** `{ "username": "bob", "level": "VIEWER" }`

---

## `GET /v1/search?q=report`

**Working logic**  
DFS; nodes without `VIEWER` are not returned, children are still walked (deeper shares).

---

## `POST /v1/nodes/{id}/move`

**Working logic**  
Sort `{source, from, dest}` by name/id; `tryLock` each; unlink; add to dest.
