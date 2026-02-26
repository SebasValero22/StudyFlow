# StudyFlow PWA - Technical Submission Documentation

## 🚀 Project Overview
StudyFlow is a modern, responsive Progressive Web App designed to help students manage their academic life. It integrates seamlessly with a Spring Boot REST API to provide a real-time, personal cloud experience.

---

## 🎯 Rubric Fulfillment (RA1 - RA8)

### RA1 & RA3: Visual Design & Modular Components
- **Modular Architecture:** The application is divided into 5 distinct functional modules (`Overview`, `Subjects`, `Tasks`, `Exams`, `Grades`), each with its own standalone component, logic, and styling.
- **Reusable Styles:** Global SCSS variables and a consistent layout (Sidebar + Content) ensure a professional look across all views.
- **Dynamic Binding:** Used `[ngStyle]` to dynamically apply subject-specific colors to task and exam cards, fulfilling aesthetic requirements.

### RA2 & RA4: Interaction & Usability
- **English UI:** 100% of the user interface is in English, including labels, placeholders, and feedback messages.
- **Error Handling:** Implemented visual feedback for API failures and loading states (spinners) to prevent user confusion (Fulfills RA2.3 & RA2.4).
- **No ID Columns:** Strict adherence to security and UX standards by hiding database primary keys (taskId, examId) from the user.
- **Responsive Design:** Mobile-first approach using CSS Flexbox and Media Queries for full compatibility with smartphones and tablets.

### RA5: Dynamic Reports
- **GPA Calculation:** A real-time reporting tool that fetches all grades and calculates a weighted average (RA5.1).
- **Notion-style Calendar:** A dynamic dashboard that aggregates data from multiple endpoints (Tasks, Exams) and filters them by a user-selected timeframe (RA5.2).

### RA6: Technical Documentation
- **API Integration:** Centralized `ApiService` using Angular's `HttpClient` for structured communication with the Spring Boot backend.
- **Interfaces:** Strictly typed TypeScript interfaces for all data models (Subject, Task, Exam, Grade) ensuring code reliability.

### RA7: Distribution & PWA
- **Installable:** Includes a `manifest.webmanifest` and service worker configuration, making the app installable on Android/iOS/Desktop.
- **Offline Support:** Service worker caches essential assets and provide basic offline availability (RA7.3).

### RA8: Testing & Validation
- **Manual Testing:** Verified all CRUD operations.
- **Error Resilience:** Tested API failure scenarios; the UI correctly displays "Failed to load" messages instead of crashing.

---

## ☁️ Personal Cloud & Synchronization
The StudyFlow PWA acts as a **Personal Cloud** similar to TickTick. Data is synchronized in real-time with the Spring Boot REST API. When a user adds a task on their phone, it is immediately available on their desktop. The PWA architecture ensures that the latest academic data is always just one tap away.

---

## 🛠️ Setup Instructions
1. Ensure the Spring Boot API is running on `http://localhost:8080`.
2. Run `npm install` in this directory.
3. Run `ng serve` to start the development server.
4. Access the app at `http://localhost:4200`.
