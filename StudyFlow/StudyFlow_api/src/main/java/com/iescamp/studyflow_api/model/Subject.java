package com.iescamp.studyflow_api.model;

import jakarta.persistence.*;
import lombok.Data;


/**
 * Mapped to 'subjects' table.
 * Represents a study category owned by a {@link User}.
 */

@Data
@Entity
@Table(name = "Subjects")
public class Subject {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer subjectId;

        @Column(name = "nameSubject", nullable = false) // Match SQL column name
        private String name;

        private String academicYear;
        private String color;

        @Column(name = "activeSubject")
        private boolean active = true;

        @ManyToOne
        @JoinColumn(name = "userId")
        private User user;
}