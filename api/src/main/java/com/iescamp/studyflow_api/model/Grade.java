package com.iescamp.studyflow_api.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Grades")
@SQLRestriction("deletedAt IS NULL")
public class Grade {
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
    private Integer gradeId;

    @ManyToOne
    @JoinColumn(name = "lifeAreaId", nullable = false)
    private LifeArea LifeArea;

    @Column(name = "userId")
    private Integer userId;

    private String concept;
    private Double score;
    private Double weight;

    @Column(name = "gradeDate")
    private LocalDate gradeDate;

    @Column(name = "clientUuid")
    private String clientUuid;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt;
}
