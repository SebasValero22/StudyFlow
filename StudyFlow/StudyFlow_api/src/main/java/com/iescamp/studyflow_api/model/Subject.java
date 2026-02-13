package com.iescamp.studyflow_api.model;

import jakarta.persistence.*;
import lombok.Data;


/**
 * Mapped to 'subjects' table.
 * Represents a study category owned by a {@link User}.
 */

@Data
@Entity
@Table(name = "subjects")
public class Subject {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer subjectId;

        @ManyToOne
        @JoinColumn(name = "userId", nullable = false)
        private User user;

        @Column(nullable = false)
        private String name;

        @Column(length = 7)
        private String color;

        private boolean active = true;

        @Column(name = "academic_year")
        private String academicYear;

}
