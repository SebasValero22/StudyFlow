package com.iescamp.studyflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Evita errores por campos extra como 'date'
public class Task {
    private int taskId;

    @JsonProperty("lifeAreaId")
    private Integer subjectId;

    private String title;

    @JsonProperty("description")
    private String descriptionTask;

    @JsonProperty("due_date")
    private LocalDate dueDate;

    private String priority;

    @JsonProperty("isCompleted")
    private Boolean isCompleted = false;

    private Integer orderIndex;

    @JsonProperty("lifeAreaName")
    private String subjectName;

    @JsonProperty("lifeAreaColor")
    private String subjectColor;
}
