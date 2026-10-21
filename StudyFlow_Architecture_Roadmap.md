# StudyFlow: Architecture & Development Roadmap
## Comprehensive Overview of Versions 1, 2, and 3

This document outlines the evolutionary phases of the **StudyFlow** ecosystem. It is designed to be parsed by AI agents to understand the system's architecture, past technical debt resolution, current production state, and future feature blueprints.

---

## 🛑 Phase 1: StudyFlow V1 (The Foundation)
**Status:** Deprecated & Refactored
**Concept:** A basic CRUD application for task and subject management.

**Technical State & Limitations:**
* **Security:** Lacked proper authentication. Relied on hardcoded fallback values (e.g., `userId = 1`) leading to a "multi-user bug" where data was not properly scoped to the individual.
* **Storage:** Basic text-only database storage without support for rich media, file attachments, or cover images.
* **Infrastructure:** Hibernate `ddl-auto=update` was used for database schema, risking data loss in production.
* **Performance:** All queries fetched entire datasets at once, with no pagination, causing memory and loading bottlenecks on mobile and web as data grew.
* **Interactivity:** Passive application. No push notifications or automated background tasks.

---

## 🟢 Phase 2: StudyFlow V2 (The Production-Ready SaaS)
**Status:** Completed & Production-Ready (Current State)
**Concept:** A secure, zero-cost, multi-platform ecosystem with local-first integrations.

**1. Bulletproof Security & Auth (JWT + OAuth2)**
* Implemented **Spring Security** with **JJWT** for stateless session management and **BCrypt** for secure password hashing.
* Enforced mandatory data filtering via custom **JPQL queries** to ensure 100% user-scoped data access.
* Implemented automated **JWT Interceptors** in the Angular PWA, Android App (OkHttp), and JavaFX Desktop client to transparently inject headers and handle 401 token expirations.
* Added a Global Exception Handler (`@ControllerAdvice`) to mask internal stack traces.

**2. "Bring Your Own Cloud" Storage Architecture**
* Integrated **Google OAuth2** to capture both Access and **Refresh Tokens**.
* Built a `GoogleDriveService` that creates a hidden `StudyFlow_Data` app-folder in the user's personal Google Drive.
* Implemented a unified `FileController` allowing users to upload PDFs and cover images directly to their Drive, keeping server storage costs at exactly $0.

**3. Cross-Platform Hardware Integration**
* **Angular PWA:** Integrated the Web Speech API for voice-to-text brainstorming, Markdown rendering (`ngx-markdown`), and multi-language support (`ngx-translate`).
* **Android (Native Java):** Implemented Android 13+ runtime permissions, FCM Push Notifications via `MyFirebaseMessagingService`, infinite scrolling via `RecyclerView` pagination, and native `RecognizerIntent` for speech-to-text.
* **Desktop (JavaFX):** Built offline image caching via `ImageIO` (saving Drive images to the local temporary directory), Dark Mode dynamic CSS injection, and native audio recording using the Java Sound API (`TargetDataLine`).

**4. Scalability & Automation Infrastructure**
* Migrated database versioning to **Flyway** (`V1__Initial_Schema.sql`) to ensure safe schema evolution.
* Implemented **Server-Side Pagination** (`PageRequest`) across backend Repositories and Controllers.
* Deployed `ReminderScheduler` (cron jobs) paired with `NotificationService` (Firebase Admin SDK & JavaMailSender) to actively push reminders 24 hours before due dates.

---

## 🚀 Phase 3: StudyFlow V3 (The Life OS & AI Ecosystem)
**Status:** Completed & Production-Ready (Current Flagship)
**Concept:** Transitioned from a task manager to the ultimate "Animedoro" study space and automated Life OS, powered by Gemini 1.5 Flash AI and deep aesthetic customization.

**1. The P.A.R.A. Backend Architecture [IMPLEMENTED]**
* Shifted from legacy `Subject` mapping to a robust `LifeArea` -> `Project` -> `Habit` architecture via Flyway schema updates.
* Universal `/api/tasks/inbox` endpoint to capture unassigned tasks globally.

**2. Global Quick Capture & Task Board [IMPLEMENTED]**
* **7-Day Visual Kanban:** Replaced vertical scrolling lists with a horizontal drag-and-drop board using `@angular/cdk/drag-drop`.
* **Frictionless Reordering:** Cross-platform drag-and-drop system leveraging the `orderIndex` database field.
* **JavaFX Spotlight Capture:** Integrated `jnativehook` for a global `Ctrl + Space` quick-capture shortcut that syncs directly to the cloud inbox without switching windows.

**3. Nyra AI Auto-Scheduling Engine [IMPLEMENTED]**
* **Gemini 1.5 Flash Integration:** Replaced local Ollama mocks with Google's Gemini API for instantaneous, strictly-structured JSON generation.
* **The "Auto-Plan" Pipeline:** Nyra reads the entire unassigned inbox, calculates temporal context, and assigns `dueDate`, `orderIndex`, and `LifeArea` based on priority.
* **Bulk Database Sync:** A single click on the PWA "✨ Auto-Plan" button commits the entire orchestrated week to MySQL via `taskRepository.saveAll()`.

**4. The "Animedoro" Focus Mechanics [IMPLEMENTED]**
* **40m/20m Timer Logic:** Built responsive countdown engines in both Angular (PWA) and JavaFX (Desktop).
* **Strict Task Locking:** Users must explicitly link a Kanban task before the timer can start.
* **AniList API Syncing:** PWA automatically queries `graphql.anilist.co` during the 20-minute break to display the user's currently watching anime covers as a reward.
* **JavaFX "Soft Lock":** Desktop app maximizes, removes decorations, and forces `AlwaysOnTop=true` to physically block distractions during the 40-minute focus block.

**5. The Deep Theme Engine [IMPLEMENTED]**
* **Global CSS Token Framework:** Replaced hardcoded hex colors with semantic CSS custom properties (`var(--bg-primary)`, etc.).
* **Instant Theme Swapping:** Zero-refresh toggling between distinct aesthetic profiles: Default Light, Midnight Tokyo (Cyberpunk), Matcha Morning (Gruvbox), and Catppuccin Mocha.
* **HTML5 Ambient Audio Core:** A persistent, minimal player embedded at the PWA root that streams native background noise (Lofi, Rain, Cafe) uninterrupted across all route navigations.

**6. Pre-Flight Security Lockdown [IMPLEMENTED]**
* **Centralized CORS:** Removed all scattered `@CrossOrigin` annotations.
* **WebMvcConfigurer:** Implemented `CorsConfig.java` to dynamically read allowed origins from environment variables (`studyflow.cors.allowed-origins`), safely restricting API access in production.