# 24-02-26 StudyFlow Changes Report

## 🖥️ StudyFlow Desktop Improvements
- **100% English UI:** Translated all Spanish labels, comments, and alert messages to English across all controllers and FXML files.
- **Enhanced Forms:** Improved the "disgusting" parts of the edit views by aligning styles and using more professional layouts.
- **Pantone Colors:** Implemented the requested Pantone color palette in the Subject form (`#FFB7B2`, `#B2E2F2`, `#B2F2BB`, `#F2F2B2`, `#E2F0CB`, `#FFDAC1`).
- **PWA Consistency:** Synchronized the TRUE/FALSE toggle button styling and logic with the PWA version for Exams and Tasks.
- **GPA Calculation:** Implemented the weighted GPA calculation in the Overview dashboard.

## ⚙️ API Hotfixes (Spring Boot)
- **CORS Configuration:** Verified and ensured `@CrossOrigin(origins = "*")` is present on all RestControllers to allow frontend connections.
- **Database Mapping:** Fixed a critical column mismatch in the `Task` entity where the `description` field was not correctly mapped to the `descriptionTask` column in the database.
- **Exam Completion:** Verified the `isCompleted` field and the `PATCH /api/exams/{id}/complete` endpoint for marking exams as finished.
- **Global Error Handling:** Confirmed the existence of a clean JSON error response handler to avoid exposing Java stack traces.

## 📱 Android App Transformation (Java Edition)
- **Java & XML Architecture:** Rebuilt the Android application using robust Java and XML layouts, ensuring full compatibility with the existing project structure.
- **Classic Navigation:** Implemented a Bottom Navigation bar using `BottomNavigationView` for intuitive switching between Dashboard, Subjects, Tasks, and Grades.
- **Secure Authentication:** Created a professional Login Activity with JWT token storage using SharedPreferences.
- **Responsive Dashboard:** Built a "Notion-style" dashboard displaying a GPA card and a `RecyclerView` of pending tasks using a custom `TaskAdapter`.
- **Data Layer:** Integrated Retrofit and Gson for seamless communication with the Spring Boot API, including a singleton `RetrofitClient` with an automated JWT interceptor.

## ✅ Final Validation
- All Spanish strings removed.
- Column mappings synchronized.
- App architecture aligned with modern standards.
