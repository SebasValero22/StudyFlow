package com.iescamp.studyflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {
    private int taskId;
    private Integer subjectId;
    private String title;

    @JsonProperty("description")
    private String descriptionTask;

    @JsonProperty("due_date")
    private LocalDate dueDate;
    
    private String priority;

    @JsonProperty("isCompleted")
    private Boolean isCompleted = false;

    public String getDaysRemaining() {
        if (dueDate == null) return "N/A";
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        if (days < 0) return "Overdue";
        if (days == 0) return "Today";
        if (days == 1) return "Tomorrow";
        return days + " days left";
    }
}