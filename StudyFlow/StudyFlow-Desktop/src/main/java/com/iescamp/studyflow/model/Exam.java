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
}