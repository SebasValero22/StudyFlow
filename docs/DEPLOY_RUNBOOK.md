# StudyFlow Deploy Runbook

Local-first architecture: the desktop machine is the source of truth. The Spring API is a replica that receives pushes and answers pulls.

## Components

| Piece | Path | Role |
|-------|------|------|
| API | `api/` | Spring Boot 4, JWT auth, `/api/sync/*`, REST for PWA |
| PWA | `pwa/` | Angular client |
| Desktop | `desktop/` | JavaFX offline-first client (SQLite outbox) |
| Linux daemon | `linux/` | Optional `studyflowd` (review-only in this workspace) |

## Required environment (API)

| Variable | Required | Notes |
|----------|----------|-------|
| `DB_URL` | yes | JDBC URL (Aiven MySQL) |
| `DB_USERNAME` | yes | |
| `DB_PASSWORD` | yes | |
| `JWT_SECRET` | yes | Fail-closed: app refuses to start without it |
| `JWT_EXPIRATION_MS` | no | Default from `application.properties` |
| `NYRA_GEMINI_KEY` | no | Optional AI feature |
| `STUDYFLOW_SEED_ADMIN_EMAIL` / `_PASSWORD` / `_NAME` | no | Seeds admin on boot |

### Local verification before deploy

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.1"
cd api
.\mvnw.cmd -B test          # expect 19+ tests green (H2, does not touch Aiven)
```

Desktop:

```powershell
cd desktop
.\mvnw.cmd -B package       # unit tests (15+) + fat jar
```

## Database migrations (Flyway)

Migrations live in `api/src/main/resources/db/migration/`.

- **V8** adds `updatedAt` / `deletedAt` tombstones + `UserSettings`.
- **V9** adds sync columns for `Exams` / `Grades` / `Reminders` and `clientUuid` on core tables.
- **V10** widens `Grades.score` / `Grades.weight` to NULL (sync may push blank grades).

**Verified 2026-09-24:** V1→V10 applied cleanly to a fresh MySQL 8 (Docker), then the API
booted against the migrated schema with `spring.jpa.hibernate.ddl-auto=validate`
(caught and fixed: table-case mismatches `users/exams/grades/files/operationhistory`
→ entities now use the PascalCase migration names; `Files` column mapping;
`OperationHistory` columns; `Grades` NOT NULL). CI job `api-migrations` repeats this
on every push.

**Risk:** V8–V10 have not been applied to live Aiven. Before enabling Flyway against prod:

1. Snapshot the database (Aiven backup / `mysqldump`).
2. Run `./mvnw flyway:info` (with prod `DB_*` env) and confirm pending versions.
3. Run `./mvnw flyway:migrate` once, then re-run `flyway:info` to confirm `current = V10`.
4. Boot the API against the migrated schema (or a restored snapshot) once — `ddl-auto=validate`
   must pass before serving traffic.
5. If a migration fails mid-way, restore the snapshot — do not hand-edit Flyway history without understanding `flyway_schema_history`.

## Deploy API (Render / any JVM host)

1. Set env vars above.
2. Build: `./mvnw -B -DskipTests package`
3. Run: `java -jar target/*.jar`
4. Health: `GET /api/auth` or any authenticated endpoint returning 401 without token (not 500).

## Sync protocol

- Protocol version: **2** (`protocolVersion` on push/pull).
- Push order: lifeAreas → tasks → focusSessions → exams → grades → reminders → settings (FK remap via `idMap`).
- Idempotency: rows carry `clientUuid`; server maps retries to the same row.
- Pull cursor: per-user ISO `serverTime` stored on the desktop (`sync.cursor.u{id}`).
- Rejections: rows failing server validation return in `rejected` (`"task:-3" → reason`);
  the desktop keeps them `PENDING_SYNC` instead of marking synced (no silent data loss).
- Conflicts: when a newer cloud row overwrites a still-pending local edit, the desktop records
  it in the local `SyncConflicts` table and shows a badge on the dashboard (`⚠ n sync conflicts`);
  the user can review and dismiss. Local newer-pending rows always win (LWW).
- Session expiry: 401 or a JWT past `exp` pauses sync (5 min) instead of spinning.

## Other clients

| Client | Auth | Sync | Verified |
|--------|------|------|----------|
| PWA (`pwa/`) | JWT via `auth.interceptor.ts` | REST only (server is source for web) | unchanged this round |
| Android (`android/`) | JWT: `auth/login` → token in `SharedPreferences` (`StudyFlowApp`), OkHttp Bearer interceptor | REST only | `assembleDebug` + `testDebugUnitTest` green (JDK 21 + Android SDK 36) |
| Linux (`linux/`) | JWT Bearer (`studyflowd`) | posts focus sessions only | `cargo check/test --workspace` green in `rust:1.98` Docker (Windows cargo is blocked by Smart App Control until reboot after registry disable) |

## Rollback

- API: redeploy previous artifact; Flyway is forward-only — restore DB snapshot if a migration must be undone.
- Desktop: users keep local SQLite; downgrade is safe as long as protocol major version is not ahead of the server (client logs a warning and continues best-effort).

## Dead code removed

- `HelloController` + `hello-view.fxml` (scaffold only)
- Empty `TypeOfReminder` enum (reminders now use message/dateTime FKs matching the API)
