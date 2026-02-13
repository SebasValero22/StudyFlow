package com.iescamp.studyflow_api.model;

import com.iescamp.studyflow_api.model.enums.Priority;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Mapped to 'tasks' table.
 * Represents an assignment linked to a specific {@link Subject}.
 */

@Data @Entity @Table(name = "tasks")
public class Task {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taskId")
    private Integer taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subjectId", nullable = false)
    private Subject subjectId;

    @Column(nullable = false)
    private String title;

    private String description;

    private LocalDate start_date;

    private LocalDate due_date;

    private Boolean isCompleted = false;

    private Priority priority;






}
