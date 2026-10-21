package com.iescamp.studyflow_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Table(name = "LifeAreas")
@SQLRestriction("deletedAt IS NULL")
public class LifeArea {

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
        @Column(name = "lifeAreaId")
        // Explicit wire key: strict bean naming would serialize getLifeAreaId() as "LifeAreaId".
        @JsonProperty("lifeAreaId")
        private Integer lifeAreaId;

        @Column(nullable = false)
        private Integer userId;

        @Column(nullable = false, name = "name")
        private String name;

        // Kept for backward compatibility with older data rows
        @Column(name = "academicYear")
        private String academicYear;

        @Column
        private String color;

        @Column(name = "active") 
        private Boolean active;

        @Column(name = "updatedAt")
        private LocalDateTime updatedAt;

        @Column(name = "deletedAt")
        private LocalDateTime deletedAt;

        @Column(name = "clientUuid")
        private String clientUuid;
}
