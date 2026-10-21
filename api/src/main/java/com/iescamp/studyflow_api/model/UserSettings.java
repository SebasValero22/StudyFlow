package com.iescamp.studyflow_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "UserSettings")
public class UserSettings {

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
    private Integer id;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "settingKey", nullable = false)
    private String settingKey;

    @Column(name = "settingValue")
    private String settingValue;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}
