# AGENTS.md — StudyFlow

## Project overview

Multi-platform student management system ("StudyFlow") with 4 independent modules under `StudyFlow-main/`:

| Module | Tech | Entry point |
|--------|------|-------------|
| `api/` | Spring Boot 4.0.2 + Java 21 + MySQL | `StudyFlowApiApplication.java` |
| `pwa/` | Angular 21.1 + TypeScript 5.9 + SCSS | `pwa/src/main.ts` |
| `desktop/` | JavaFX 19 + Java 17 | `desktop/src/.../Launcher.java` |
| `android/` | Android (minSdk 26) + Gradle + Kotlin DSL | `android/app/` |

All clients (PWA, Desktop, Android) consume the same REST API.

## Database

- **MySQL on Aiven cloud**. Schema auto-created/updated by Hibernate (`ddl-auto=update`).
- `StudyFlow.sql` at repo root is the reference DDL — do not run it manually unless recreating from scratch.
- Custom Hibernate naming strategy: `PhysicalNamingStrategyStandardImpl` (column names match Java field names exactly, no snake_case conversion).

## Developer commands

### API (`StudyFlow-main/api/`)
```
./mvnw spring-boot:run          # dev server on port 8888
./mvnw clean package            # build JAR
./mvnw test                     # run tests
```

### PWA (`StudyFlow-main/pwa/`)
```
npm install                     # install deps (npm@11.6.2)
npm start                       # ng serve (dev server)
npm run build                   # production build (includes service worker)
npm run watch                   # dev build with watch
npm test                        # Vitest via Angular builder
```

### Desktop (`StudyFlow-main/desktop/`)
```
./mvnw javafx:run               # run JavaFX app
./mvnw clean package            # build
```

### Android (`StudyFlow-main/android/`)
```
./gradlew assembleDebug         # build debug APK
./gradlew test                  # run unit tests
```

## Architecture notes

- **API port is 8888** (non-standard). Desktop `ApiClient` and Android Retrofit base URLs must match.
- **API layer**: controllers → services → repositories (Spring Data JPA). DTOs used for user registration/login; other entities returned directly.
- **Desktop**: uses Jackson for JSON, FXML views under `src/main/resources/.../fxml/`. `UserSession` utility holds logged-in user state. `ViewSwitcher` handles navigation.
- **PWA**: Angular standalone components. Feature modules under `src/app/features/`, core services under `src/app/core/`. Service worker configured via `ngsw-config.json`.
- **No shared code** between modules — each has its own model/entity definitions. Keep them in sync manually.

## Conventions

- PWA: Prettier — `printWidth: 100`, `singleQuote: true`, Angular parser for HTML.
- API: Lombok on all models (getters/setters via annotations).
- Desktop: Lombok + FXML controller pattern.

## Gotchas

- No CI/CD, no pre-commit hooks, no linting config beyond Prettier in PWA.
- No `.env` files — API credentials are hardcoded in `application.properties`. Do not commit changes to these credentials.
- Desktop `pom.xml` uses Java 17, API uses Java 21 — ensure correct JDK when switching modules.
- Tests use Vitest with globals in PWA (`vitest/globals` in tsconfig.spec.json).
