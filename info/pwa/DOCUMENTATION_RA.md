# 📄 StudyFlow - Rubric Justification (RA1 - RA8)

This document explains how the StudyFlow project fulfills the Learning Outcomes (Resultados de Aprendizaje) required for the Intermodular Project.

---

## RA1. Generates user interfaces using visual editors
- **Criterion RA1.2 (Structural organization):** Both the PWA and Desktop apps follow a professional layout hierarchy. The interface is divided into a `Sidebar` (navigation), `Header` (status/profile), and `Content Area` (functional views).
- **Criterion RA1.4 (Separation between interface and logic):** 
    - **PWA:** Uses Angular standalone components where the HTML template is strictly for presentation and the `.ts` class handles logic.
    - **Desktop:** Uses FXML files for the UI and Java Controller classes for the behavior.

## RA2. Generates natural interfaces using visual tools
- **Criterion RA2.2 (Navigation coherence):** The sidebar provides a consistent navigation flow. The "SF StudyFlow" logo acts as a global "Home" button in both platforms, returning the user to the Dashboard.
- **Criterion RA2.3 (State management):** The application handles data states gracefully. In the PWA, `cdr.detectChanges()` is used to ensure the UI updates the moment data is fetched from the API.

## RA3. Creates visual components employing specific tools
- **Criterion RA3.1 (Reusable components):** Feature modules (Tasks, Exams, etc.) are built as modular components that share a global styling system (`styles.scss` / `global.css`).
- **Criterion RA3.3 (Framework use):** Built using **Angular 19** (web) and **JavaFX 21** (desktop), utilizing modern features like signals and modular path resolution.

## RA4. Designs interfaces with usability and accessibility criteria
- **Criterion RA4.1 (Clarity and visual coherence):** A unified color palette (Dark Blue #2c3e50 and Accent Blue #3498db) is applied across both applications to ensure a professional, cohesive brand identity.
- **Criterion RA4.3 (Error prevention):** Forms in both applications validate that required fields (like Subject Name or Task Title) are filled before allowing the "Save" action.

## RA5. Creates reports using graphical tools
- **Criterion RA5.1 (Information selection):** The **Dashboard (Overview)** filters and selects only relevant "Upcoming" data, showing the user exactly what needs attention in the next 1-4 weeks.
- **Criterion RA5.2 (Visual design of reports):** The **GPA Calculation** is a real-time report that aggregates weighted grades into a single, prominent visual indicator.

## RA6. Documents applications using specific tools
- **Criterion RA6.1 (Technical documentation):** Detailed technical guides are provided in `README.md` and `COMPLETION_REPORT.md`.
- **Criterion RA6.3 (Professional tools):** Documentation is written in **Markdown**, following industry standards for software repositories.

## RA7. Prepares applications for distribution
- **Criterion RA7.1 (PWA Readiness):** The web application is a fully functional **Progressive Web App** with a Service Worker and `manifest.webmanifest`, making it installable on mobile and desktop.
- **Criterion RA7.2 (External configuration):** API endpoints and database credentials are managed in centralized configuration files (`application.properties` and `api.ts`).

## RA8. Evaluates application behavior by designing tests
- **Criterion RA8.2 (API interaction tests):** The project was tested against various API scenarios, including handling "500 Internal Server Errors" by implementing a **Global Exception Handler** in the Spring Boot backend.
- **Criterion RA8.3 (Error management):** Identified and resolved critical runtime errors, such as database column mismatches and JDK compatibility issues, ensuring a stable final build.
