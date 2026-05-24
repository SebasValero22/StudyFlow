package com.iescamp.studyflow_api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "exams")
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer examId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subjectId", nullable = false)
    private Subject subject;

    @Column(name = "nameExam", nullable = false)
    private String name;

    // Asegurate de que esto sea String
    @Column(name = "examType")
    private String examType;

    @Column(name = "examDate")
    private LocalDate examDate;

    @Column(name = "classroom")
    private String classroom;

    @Column(name = "isCompleted")
    private Boolean isCompleted = false;
}
