package com.iescamp.studyflow_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Subjects")
public class LifeArea {

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
}