package com.iescamp.studyflow_api.model;

import com.iescamp.studyflow_api.model.enums.Mood;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "VoiceJournal")
@SQLRestriction("deletedAt IS NULL")
public class VoiceJournal {

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
    @Column(name = "entryId")
    private Integer entryId;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "taskId")
    private Integer taskId; // Optional link; DB uses ON DELETE SET NULL

    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "moodTag")
    private Mood moodTag;

    @Column(columnDefinition = "TEXT")
    private String rawTranscript;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    @Column(name = "audioPath")
    private String audioPath;

    @Column(name = "durationSeconds")
    private Integer durationSeconds;

    @CreationTimestamp
    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt;
}
