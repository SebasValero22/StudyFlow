# Bring Your Own Cloud (BYOC)

StudyFlow is designed so the server is interchangeable: run the Spring Boot API
anywhere, point any client at it, log in. No rebuilds needed.

## 1. Host your API

```bash
cd api
export DB_URL='jdbc:mysql://<host>:<port>/<db>?ssl-mode=REQUIRED'
export DB_USERNAME='<user>'
export DB_PASSWORD='<password>'
export JWT_SECRET="$(openssl rand -base64 64)"   # required, >= 32 bytes
./mvnw spring-boot:run
```

On first boot the API **seeds `admin@mail.com` / `123`** automatically
(BCrypt-hashed, idempotent). Override or disable via env:

| Variable | Default | Purpose |
|---|---|---|
| `STUDYFLOW_SEED_ADMIN_ENABLED` | `true` | set `false` to disable seeding |
| `STUDYFLOW_SEED_ADMIN_EMAIL` | `admin@mail.com` | seed account email |
| `STUDYFLOW_SEED_ADMIN_PASSWORD` | `123` | **change immediately after login** |
| `NYRA_GEMINI_KEY` | mock mode | real Nyra AI when set |
| `JWT_EXPIRATION_MS` | `86400000` | token lifetime |

Flyway migrates the schema itself (`V1` → `V7+`); the bundled Aiven/MySQL
dialect also works against MariaDB and self-hosted MySQL 8.

## 2. Point the clients at it

| Client | Where | How |
|---|---|---|
| Desktop (JavaFX) | Settings → Your Cloud | paste `https://your-server/api`, hit **Test** (calls `/api/public/ping`), Save, re-login |
| Linux daemon | `~/.config/studyflow/config.toml` | set `api_url`, then `studyflow-cli login --email … --password …` |
| PWA / Android | build-time `api.url` / BuildConfig | redeploy with the new base URL |

## 3. Reachability check (no auth needed)

```
GET {api-url}/public/ping   →  {"status":"ok","app":"StudyFlow_api","time":"…"}
```

Use it from scripts, health checks, or the desktop Test button before logging in.

## 4. File storage per deployment

`studyflow.storage.provider` (default `local`):
- `local` → files under `./data/files/<userId>/` on the API host. Zero-config,
  perfect for a home server or single VPS.
- Google Drive vault support is designed as a drop-in `StorageService`
  implementation for multi-device setups (OAuth credentials required).

## 5. Production checklist

- [ ] Change the seeded admin password (or disable seeding)
- [ ] Strong `JWT_SECRET` (never the dev default)
- [ ] DB backups (migrations are forward-only)
- [ ] HTTPS in front of the API (Caddy/Nginx), CORS origins tightened
- [ ] `api/data` (uploaded voice notes) included in backups
