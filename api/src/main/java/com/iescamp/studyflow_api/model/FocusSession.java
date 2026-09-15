package com.iescamp.studyflow_api.model;

import com.iescamp.studyflow_api.model.enums.SessionMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "FocusSessions")
public class FocusSession {

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
}
