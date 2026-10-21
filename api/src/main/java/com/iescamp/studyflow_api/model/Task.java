package com.iescamp.studyflow_api.model;

import com.iescamp.studyflow_api.model.enums.Priority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deletedAt IS NULL")
public class Task {

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
    @Column(name = "taskId")
    private Integer taskId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lifeAreaId", nullable = true)
    private LifeArea lifeArea;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "projectId", nullable = true)
    private Project project;

    @Column(nullable = false)
    private String title;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "descriptionTask")
    private String description;

    // Explicit wire names: V1 schema uses startDate/dueDate (camelCase), not the field names.
    @Column(name = "startDate")
    private LocalDate start_date;

    @Column(name = "dueDate")
    private LocalDate due_date;

    @Column(name = "isCompleted")
    private Boolean completed = false;

    @Column(name = "isHabit")
    private Boolean isHabit = false;

    @Column(name = "orderIndex")
    private Integer orderIndex = 0;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    // Implementation Intention fields (RAWFOCUS)
    @Column(name = "trigger")
    private String trigger;

    @Column(name = "microStep")
    private String microStep;

    @Column(name = "energyBucket")
    private String energyBucket;

    public Boolean getIsCompleted() {
        return completed;
    }

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt;

    @Column(name = "clientUuid")
    private String clientUuid;
}
