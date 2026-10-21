package com.iescamp.studyflow_api.model;

import com.iescamp.studyflow_api.model.enums.SessionMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "FocusSessions")
@SQLRestriction("deletedAt IS NULL")
public class FocusSession {

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
    @Column(name = "sessionId")
    private Integer sessionId;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "taskId")
    private Integer taskId;

    @Column(name = "startedAt", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "endedAt", nullable = false)
    private LocalDateTime endedAt;

    @Column(name = "durationSeconds", nullable = false)
    private Integer durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private SessionMode mode = SessionMode.FOCUS;

    @Column(nullable = false)
    private Boolean completed = true;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt;

    @Column(name = "clientUuid")
    private String clientUuid;
}
