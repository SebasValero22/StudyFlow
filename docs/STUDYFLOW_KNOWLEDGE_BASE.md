# StudyFlow Knowledge Base & Documentation

Welcome to the central repository for StudyFlow's architectural history, UI design patterns, platform-specific logic, and consolidation reports. This file replaces multiple older fragmented markdown files to provide a single, structured source of truth for future AI sessions and developers.

---

## 🏗️ Project Consolidation & Infrastructure

The project has been successfully consolidated into a single unified directory `studyflow_main` combining the most up-to-date versions of all clients:
- **API (Spring Boot):** Connects to the Aiven MySQL database (`mysql-24d1d64a-studyflowddbb.g.aivencloud.com`).
- **Android App:** Configured to point to the Render API (`https://studyflow-nuux.onrender.com/api/`). Local emulator testing uses `10.0.2.2:8888`.
- **Desktop App (JavaFX):** Configured via `config.properties` to hit the Render API.
- **PWA (Angular):** Connects to the Render API.

---

## 🎨 UI & Design Principles: Subject Color Logic

The "Subject Color" feature is central to StudyFlow's visual identity. Items are styled based on the color assigned to the user's `Subject`.
- **API Data:** `TaskResponseDTO` and `ExamDTO` automatically include `subjectColor` and `subjectName` mapped from the related `Subject` entity, preventing the need for extra API calls.
- **Desktop (JavaFX):** Uses custom Cell Factories in `TaskOverviewController` and `ExamsController` to color the title column based on `subjectColor`. Subject views apply the hex color directly to the name row.
- **Android:** Uses customized Adapters (`SubjectAdapter`, `TaskAdapter`) to apply the color directly to the item text UI.

---

## 💻 Platform Modifications Summary

### 1. JavaFX Desktop Application
- **Filtering System:** Implemented "Filter by Subject" ComboBoxes in both `tasks_overview.fxml` and `exams_view.fxml`. Leverages `FilteredList` and `SortedList`.
- **Grade Management:** Created a dedicated `grade_form.fxml` to add/edit grades. Deletions have confirmations, and the UI dynamically recalculates weighted GPAs.
- **UI Enhancements:** Migrated the app completely to 100% English. Improved forms with Pantone pastel color palettes for subjects (`#FFB7B2`, `#B2E2F2`, `#B2F2BB`, etc.).
- **Consistency:** Synchronized TRUE/FALSE toggle buttons with PWA styling logic.

### 2. Android Application (Native Java)
- **Architecture Updates:** Migrated to **Java 21**. Separated database logic with a dedicated `com.example.studyflow.data.dto` package mirroring the API.
- **Navigation:** Implemented a `BottomNavigationView` for modern navigation. The top toolbar utilizes a back-arrow stack via `OnBackPressedDispatcher`.
- **Authentication:** Created secure `LoginActivity` and `RegisterActivity` saving JWTs to `SharedPreferences`. User sessions are validated automatically in `MainActivity`.
- **Task Workflow:** Added selection mechanics with gray highlight. Tasks can be edited by selecting them to reveal a top edit icon or via a long-press shortcut. 
- **Consistency:** Utilizes consistent Blue Floating Action Buttons (FABs) with "+" icons for all creation screens (Tasks, Subjects, Grades).

### 3. Progressive Web App (Angular)
- **Phase 3 Kanban Implementation:** The traditional table view has been replaced with a horizontal Drag-and-Drop Kanban board using `@angular/cdk/drag-drop`.
- **Intelligent Sorting:** Tasks are categorized into "Someday" (null due dates), "Today", "Tomorrow", "Later", and "Completed".
- **Dynamic Reordering:** Reordering fires a `PATCH /api/tasks/reorder` to immediately sync the `orderIndex` and column state with the database.

---

## 🧠 StudyFlow V3: Nyra AI & Deep Theme Engine

StudyFlow V3 introduces a massive leap in functional intelligence and aesthetic immersion.

### 1. Nyra AI Orchestrator (Backend)
- **Local/Remote LLM Integration:** `NyraService.java` is wired to accept either local models (e.g., Ollama) or remote APIs (e.g., Gemini 1.5 Flash).
- **Intent Routing (`/api/nyra/ingest`):** Accepts `multipart/form-data` (text or audio Blobs) from the frontend, parses the natural language using strict JSON schema prompts, and automatically creates tasks in the database.
- **Auto-Schedule Engine (`/api/nyra/auto-schedule`):** Analyzes unassigned inbox tasks (`due_date IS NULL`) and uses the LLM to automatically assign logical due dates, priorities, and life areas, saving the results in bulk.

### 2. Voice UI (Frontend)
- **Global Action Button:** An Angular standalone component (`NyraVoiceButton`) uses the browser's `MediaRecorder` API to capture microphone input globally across the PWA.

### 3. Deep Theme Engine & Ambiance Core
- **Dotfile-Style CSS Variables:** All hardcoded colors in the PWA have been replaced by CSS Custom Properties (`--bg-primary`, `--accent-primary`, etc.) mapped in `themes.scss`.
- **Dynamic Theming:** The `ThemeService` saves preferences to `localStorage` and injects `data-theme` attributes (e.g., `cyberpunk`, `lofi-cafe`, `catppuccin`) into the `:root` document to hot-swap aesthetics without reloading.
- **Ambiance Player:** An HTML5 `<audio>` engine driven by `AudioService` loops background tracks (e.g., Rain, Cafe, Campfire) globally, controlled by a minimal UI component in the layout.

### 4. Nyra AI (Spring Boot Backend)
- **Gemini 1.5 Flash:** `NyraService.java` orchestrates prompts mapping unassigned Kanban items. Strict JSON output generation ensures reliable backend bulk syncing via `taskRepository.saveAll()`.

---

## ⚙️ API Hotfixes & Backend Rules

- **CORS Configuration:** Enforced `@CrossOrigin(origins = "*")` globally.
- **Entity Mappings:** Fixed column mismatch where `description` was mapped to `descriptionTask`. All mappings must be strictly verified against the database schema.
- **Error Handling:** Exists a Global Error Handler (`@ControllerAdvice`) preventing stack traces from leaking to the frontend.

---
*Generated by Antigravity during the Phase 3 Kickoff session.*
