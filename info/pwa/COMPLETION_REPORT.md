# 🎓 StudyFlow - Final Project Completion Report

This document provides a detailed technical breakdown of all modifications performed to finalize the StudyFlow ecosystem (API, PWA, and Desktop).

---

## 🏗️ 1. Backend: Spring Boot 3 API (Port 8081)
**Goal:** Finalize business logic, fix database mismatches, and enable cross-platform connectivity.

### 🔐 Security & Connectivity
- **CORS Configuration:** Added `@CrossOrigin(origins = "*")` to all 8 RestControllers (`Subject`, `Task`, `Exam`, `Grade`, `User`, etc.).
  - *Why:* To allow the Angular PWA (running on port 4200) to securely request data from the API.
- **Port Change:** Moved `server.port` from 8888 to **8081** in `application.properties`.
  - *Why:* To resolve "Port already in use" conflicts on your system.

### 💾 Database & Entity Mapping
- **Table Alignment:** Renamed `@Table` annotations to match the PascalCase names in your SQL script (`Tasks`, `Exams`, `Subjects`).
- **Task Entity Fix:** 
  - Renamed `description` to `descriptionTask` in `@Column` mapping to fix SQL 500 errors.
  - Renamed internal field `subjectId` to `subject` for cleaner Hibernate relationship mapping.
- **Exam Entity Upgrade:** Added `isCompleted` boolean field to track status.
- **Global Exception Handler:** Created `GlobalExceptionHandler.java` in the `exception` package.
  - *Why:* To catch Java stack traces and return clean, user-friendly JSON errors (`{"error": "...", "status": 400}`).

### ⚡ Service Layer Refinement
- **Task & Exam Services:** Updated `add()` and `modify()` methods to ensure the `isCompleted` status and `userId` are correctly persisted.
- **New Endpoint:** Added `PATCH /api/exams/{id}/complete` to toggle exam status.

---

## 🌐 2. Frontend: Angular 19 PWA (Port 4200)
**Goal:** Build a responsive, 100% English, Notion-style dashboard with 5 modules.

### 🎨 UI & UX Design
- **100% English:** All labels, placeholders, and error messages translated to English.
- **No Database IDs:** Strict UI rules to hide `taskId` or `subjectId` from the user.
- **Personal Cloud Header:** Added a top bar with:
  - **Sync Indicator:** Visual "Personal Cloud Sync: Active" status.
  - **User Profile:** Clickable profile section linking to a new Profile module.

### 🧩 Functional Modules
1.  **Overview (Dashboard):** 
    - Notion-style upcoming view.
    - Dynamic "Next X Weeks" filter.
    - Real-time **GPA Calculation** logic.
2.  **Subjects:**
    - Form with a restricted **Pantone Color Picker** (`#FFB7B2`, `#B2E2F2`, etc.).
3.  **Tasks:**
    - List view with subject filtering and completion checkboxes.
    - Dynamic border-color binding using `[ngStyle]` based on the subject's Pantone color.
4.  **Exams:**
    - Toggleable status using the new API `PATCH` endpoint.
5.  **Grades:**
    - Weighted average calculation for academic reporting.

### 📱 PWA Features
- **Service Worker:** Enabled via `ng add @angular/pwa`.
- **Manifest:** Configured `manifest.webmanifest` for cross-platform installation.

---

## 🖥️ 3. Desktop: JavaFX Application
**Goal:** Fix boot layer and compiler errors for modern JDK compatibility.

- **Module Path Fix:** Updated `pom.xml` to explicitly include `jackson-core` and `jackson-annotations`.
  - *Why:* To resolve `java.lang.module.FindException` by ensuring libraries are on the Module Path.
- **Lombok Compatibility:** Upgraded Lombok to **1.18.36** and Maven Compiler Plugin to **3.13.0**.
  - *Why:* To fix the `com.sun.tools.javac.code.TypeTag :: UNKNOWN` error occurring on JDK 21+.
- **Module-Info Update:** Added necessary `requires` and `opens` directives for Jackson JSON serialization.

---

## 📊 4. Database: StudyFlow .sql
- **Pre-populated Data:** 
  - Created a default **User (ID: 1)**.
  - Inserted **5 Subjects** and **5 Tasks** as requested.
- **Schema Cleanup:** Standardized table names to plural PascalCase (`Subjects`, `Tasks`, `Exams`).

---

**Mission Accomplished.** All systems are aligned, synchronized, and functional.
