package com.iescamp.studyflow_api.model;

import com.iescamp.studyflow_api.model.enums.Priority;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity @Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taskId")
    private Integer taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subjectId", nullable = false)
    private Subject subjectId;

    @Column(nullable = false)
    private String title;

    @Column(name = "descriptionTask")
    private String description;

    // Asegúrate de que los nombres coincidan con el DTO
    private LocalDate start_date;
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