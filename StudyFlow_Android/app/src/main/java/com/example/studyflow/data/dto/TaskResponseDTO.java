package com.example.studyflow.data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskResponseDTO {
    private Integer taskId;
    private String title;
    private String descriptionTask;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate due_date;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate start_date;
    
    private String priority;
    private Boolean isCompleted;
    private Integer subjectId;

    // Getters and Setters
    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescriptionTask() { return descriptionTask; }
    public void setDescriptionTask(String descriptionTask) { this.descriptionTask = descriptionTask; }
    public LocalDate getDue_date() { return due_date; }
    public void setDue_date(LocalDate due_date) { this.due_date = due_date; }
    public LocalDate getStart_date() { return start_date; }
    public void setStart_date(LocalDate start_date) { this.start_date = start_date; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
}
