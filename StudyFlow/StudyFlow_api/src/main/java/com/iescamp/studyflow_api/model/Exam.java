package com.iescamp.studyflow_api.model;

import com.iescamp.studyflow_api.model.enums.ExamType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "Exams")
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer examId;

    @Column(name = "nameExam", nullable = false) // Match SQL column name
    private String name;

    private ExamType examType;
    private java.sql.Date examDate;
    private String classroom;

    @ManyToOne
    @JoinColumn(name = "subjectId")
    private Subject subject;
}
