package com.iescamp.studyflow_api.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "Reminders")
@SQLRestriction("deletedAt IS NULL")
public class Reminder {
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
    private Integer reminderId;

    @Column(name = "userId")
    private Integer userId;

    private String message;

    @Column(name = "date_Time")
    private Date dateTime;

    @ManyToOne
    @JoinColumn(name = "taskId", nullable = true)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "examId", nullable = true)
    private Exam exam;

    @Column(name = "clientUuid")
    private String clientUuid;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt;
}
