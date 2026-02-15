package com.iescamp.studyflow_api.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Subjects")
public class Subject {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer subjectId;

        @Column(nullable = false)
        private Integer userId;

        @Column(nullable = false)
        private String nameSubject;

        // CAMBIO CLAVE: String
        @Column(name = "academicYear")
        private String academicYear;

        @Column
        private String color;

        @Column(name = "activeSubject") // Asegura que coincida con tu SQL
        private Boolean activeSubject;
}