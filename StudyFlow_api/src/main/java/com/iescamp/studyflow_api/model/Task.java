package com.iescamp.studyflow_api.model;

import com.iescamp.studyflow_api.model.enums.Priority;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity @Table(name = "Tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taskId")
    private Integer taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subjectId", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private String title;

    @Column(name = "descriptionTask")
    @JsonProperty("description")
    private String description;

    @Column(name = "startDate")
    private LocalDate start_date;

    @Column(name = "dueDate")
    private LocalDate due_date;

    // Cambiamos el nombre del campo para evitar conflictos de Lombok
    @Column(name = "isCompleted")
    private Boolean completed = false;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    // Getter manual por si Lombok genera "isCompleted()" en lugar de "getCompleted()"
    public Boolean getIsCompleted() {
        return completed;
    }
}