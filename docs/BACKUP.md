# StudyFlow Backup Strategy

## What must be backed up

| Data | Where it lives | Priority |
|------|----------------|----------|
| Production MySQL | Aiven (API) | **Critical** — users' cloud replica |
| Desktop SQLite | `~/StudyFlow/vault/studyflow.db` (or `notes.vaultDir` override) | **Critical** — machine is source of truth |
| Notes vault | Same directory as SQLite (markdown files) | High |
| Secrets | Host env (`JWT_SECRET`, `DB_*`) | Critical — store in a password manager, not git |

## Database (Aiven MySQL)

- Enable Aiven's automated backups (default retention applies).
- Before Flyway migrations (V8/V9): take a manual snapshot.
- Optional weekly logical dump:

```bash
mysqldump --single-transaction --routines \
  -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USERNAME" -p"$DB_PASSWORD" \
  "$DB_NAME" | gzip > "studyflow-$(date +%F).sql.gz"
```

Store dumps off-machine (separate disk or object storage with versioning).

## Desktop (local-first)

The desktop SQLite file **is** the user's primary copy when offline.

- Copy `studyflow.db` (and notes `.md` files) from the vault directory while the app is closed (avoids WAL torn reads).
- Suggested: weekly copy to external drive / synced folder that is **not** the vault itself.
- After a successful sync session (status shows SYNCED), the cloud also holds a replica — still keep a local backup for ransomware/corruption.

### Quick Windows backup

```powershell
$vault = Join-Path $env:USERPROFILE "StudyFlow\vault"
$dest  = "D:\Backups\StudyFlow\$(Get-Date -f yyyy-MM-dd)"
New-Item -ItemType Directory -Force -Path $dest | Out-Null
Copy-Item "$vault\*" -Destination $dest -Recurse
```

## Restore checks (do this once a year)

1. Restore MySQL dump into a scratch schema; boot API with `DB_URL` pointed at it; login works.
2. Copy a desktop backup into a temp vault; start desktop with `studyflow.vault=<temp>`; tasks/exams appear offline.

## What is not backed up by design

- Java `Preferences` (registry) — only UI theme/timer/cloud URL overrides; defaults are fine.
- JWT sessions — users simply log in again.
