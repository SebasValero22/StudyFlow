# StudyFlow

**Your personal study companion. Organize, learn, and progress.**

StudyFlow is a multi-platform academic management system designed to streamline your studies. It features a Spring Boot API, an Android application, a JavaFX Desktop application, and an Angular Progressive Web App (PWA), all connected to a cloud-hosted MySQL database.

---

## 🚀 Project Components

- **[API](./api)**: Spring Boot RESTful service (deployed on Render).
- **[Android](./android)**: Native Android application with Retrofit integration.
- **[Desktop](./desktop)**: JavaFX desktop application for advanced management.
- **[PWA](./pwa)**: Angular-based Progressive Web App (deployed on Netlify).
- **[Database](./database)**: MySQL schema and test data (hosted on Aiven).

---

## 🌐 Production Links

- **Frontend (PWA):** [https://studyflow-pwa.netlify.app](https://studyflow-pwa.netlify.app)
- **Backend (API):** [https://studyflow-nuux.onrender.com](https://studyflow-nuux.onrender.com)
- **Repository:** [https://github.com/SebasValero22/StudyFlow](https://github.com/SebasValero22/StudyFlow)

---

## 📁 Directory Structure

```text
StudyFlow/
├── android/    # Android Studio project (Java)
├── api/        # Spring Boot API project (Java 21)
├── assets/     # Images, icons, and logos
├── database/   # SQL scripts and DB documentation
├── desktop/    # JavaFX Desktop application
├── docs/       # Historical documentation and diagrams
└── pwa/        # Angular Progressive Web App
```

---

## 🛠️ Key Features

- **Centralized Management:** Sync tasks, exams, and subjects across all your devices.
- **Subject Color Logic:** Automatically styles your tasks and exams based on the color assigned to the subject.
- **Grade Tracking:** Calculate weighted averages and track your academic progress.
- **Cloud-Native:** Seamlessly integrated with Aiven MySQL and Render API.

---

## 📖 Documentation

For detailed architecture, deployment instructions, and development guidelines, please refer to:
- [Project Instructions (GEMINI.md)](./GEMINI.md)
- [Documentation Folder](./docs)

---

**Author:** Sebastian Valero  
**Project:** Final Project - IES Camp de Morvedre
