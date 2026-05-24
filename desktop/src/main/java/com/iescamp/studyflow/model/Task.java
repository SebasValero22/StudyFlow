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

    @JsonProperty("due_date")
    private LocalDate dueDate;

    private String priority;

    @JsonProperty("isCompleted")
    private Boolean isCompleted = false;

    private String subjectName;
    private String subjectColor;

    @JsonProperty("isCompleted")
    public Boolean getIsCompleted() {
        return isCompleted;
    }

    @JsonProperty("isCompleted")
    public void setIsCompleted(Boolean completed) {
        this.isCompleted = completed;
    }
}