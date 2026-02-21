package com.iescamp.studyflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Evita errores por campos extra como 'date'
public class Task {
    private int taskId;
    private Integer subjectId;
    private String title;

    @JsonProperty("description") // <--- ESTO SOLUCIONA EL MAPEO
    private String descriptionTask;

    private LocalDate dueDate;
    private String priority;
    private Boolean isCompleted;
}