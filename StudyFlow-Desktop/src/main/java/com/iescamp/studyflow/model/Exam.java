package com.iescamp.studyflow.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Exam {
    private int examId;
    private int subjectId;
    private String nameExam; // Coincide con colExamName -> "nameExam"
    private String examType; // Coincide con colExamType -> "examType"
    private LocalDate examDate; // Coincide con colDate -> "examDate"
    private String classroom; // Coincide con colRoom -> "classroom"
    private Boolean isCompleted = false;

    public String getDaysRemaining() {
        if (examDate == null) return "N/A";
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), examDate);
        if (days < 0) return "Overdue";
        if (days == 0) return "Today";
        if (days == 1) return "Tomorrow";
        return days + " days left";
    }
}