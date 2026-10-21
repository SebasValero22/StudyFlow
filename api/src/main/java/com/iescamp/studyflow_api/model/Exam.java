package com.iescamp.studyflow_api.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Exams")
@SQLRestriction("deletedAt IS NULL")
public class Exam {
    @PrePersist
    void onCreate() {
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer examId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LifeAreaId", nullable = false)
    private LifeArea LifeArea;

    @Column(name = "userId")
    private Integer userId;

    @Column(name = "nameExam", nullable = false)
    private String name;

    @Column(name = "examType")
    private String examType;

    @Column(name = "examDate")
    private LocalDate examDate;

    @Column(name = "classroom")
    private String classroom;

    @Column(name = "clientUuid")
    private String clientUuid;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt;
}
