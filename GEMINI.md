# StudyFlow - Project Instructions

## 🏛️ Architecture Overview

StudyFlow follows a client-server architecture with multiple client types:

- **Backend:** Spring Boot 4.0.2 API (Java 21) using Spring Data JPA.
- **Persistence:** MySQL 8.x (hosted on Aiven).
- **Desktop Client:** JavaFX (OpenJFX) with custom CSS styling.
- **Mobile Client:** Native Android (minSdk 26) using Retrofit and Jackson.
- **Web Client:** Angular 19+ Progressive Web App (PWA).

## 🔧 Infrastructure & Connectivity

### API (Render)
- **Base URL:** `https://studyflow-nuux.onrender.com/api`
- **Port:** `8888` (internal)
- **CORS:** Enabled for all origins (`@CrossOrigin`).

### Database (Aiven)
- **Host:** `mysql-24d1d64a-studyflowddbb.g.aivencloud.com`
- **Port:** `27865`
- **Database:** `defaultdb` (or `StudyFlow`)
- **SSL:** Required (`ssl-mode=REQUIRED`).

## 🎨 Design & Styling Conventions

### Subject Color Logic
The "Subject Color" is a core feature. Each `Subject` has a `color` hex field.
- **API:** The `TaskResponseDTO` and `ExamDTO` include a `subjectColor` field, automatically populated from the associated `Subject` entity.
- **Android:** Use `TaskAdapter` and `SubjectAdapter` to apply the `subjectColor` to text views.
- **Desktop:** Custom `CellFactory` implementations in JavaFX controllers (e.g., `TaskOverviewController`, `ExamsController`) apply the hex color as an inline `-fx-text-fill` style.

## 🚀 Deployment Workflow

### API
1. **Docker:** Use the provided `Dockerfile` in the `api/` folder.
2. **Environment Variables:**
   - `SPRING_DATASOURCE_URL`: `jdbc:mysql://<host>:<port>/<db>?ssl-mode=REQUIRED`
   - `SPRING_DATASOURCE_USERNAME`: `avnadmin`
   - `SPRING_DATASOURCE_PASSWORD`: `<password>`

### PWA
1. **Build:** `ng build --configuration production`
2. **Netlify:** Deploy the contents of `dist/pwa/browser`.
3. **Routing:** Ensure `_redirects` file is present in the public root for SPA routing.

### Android
1. **Java Version:** Use Java 21.
2. **API Connection:** Ensure `ApiClient.java` uses the production Render URL. For local emulator testing, use `10.0.2.2:8888`.
3. **Keystore:** Signed APKs should use the key located in `android/keystore/studyflow_androidKey`.

## 📜 Coding Standards
- **Language:** English (Code, Comments, and UI).
- **Naming:** CamelCase for Java/TS, snake_case for database columns (if mapped) or matching DTO names.
- **Warnings:** Maintain zero compilation warnings and zero lint errors.
