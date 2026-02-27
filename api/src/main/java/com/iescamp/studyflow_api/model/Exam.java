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

    // ASEGÚRATE DE QUE ESTO SEA STRING
    @Column(name = "examType")
    private String examType;

    @Column(name = "examDate")
    private LocalDate examDate;

    @Column(name = "classroom")
    private String classroom;
}