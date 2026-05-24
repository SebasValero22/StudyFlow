package com.example.studyflow.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskResponseDTO {
    private Integer taskId;
    private String title;
    private String description;
    
    private LocalDate due_date_field;
    private LocalDate start_date_field;
    
    private String priority;
    private Boolean isCompleted;
    private Integer subjectId;
    private String subjectName;
    private String subjectColor;

    // Setters especiales para manejar fechas del backend (compatibles con arrays, strings y epoch)
    @JsonProperty("due_date")
    public void setDue_date(Object date) {
        this.due_date_field = parseDate(date);
    }

    @JsonProperty("start_date")
    public void setStart_date(Object date) {
        this.start_date_field = parseDate(date);
    }

    private LocalDate parseDate(Object date) {
        if (date == null) return null;
        if (date instanceof LocalDate) {
            return (LocalDate) date;
        }
        if (date instanceof String) {
            try {
                return LocalDate.parse((String) date);
            } catch (Exception e) {
                return null;
            }
        }
        if (date instanceof java.util.List) {
            try {
                java.util.List<?> list = (java.util.List<?>) date;
                if (list.size() >= 3) {
                    int year = ((Number) list.get(0)).intValue();
                    int month = ((Number) list.get(1)).intValue();
                    int day = ((Number) list.get(2)).intValue();
                    return LocalDate.of(year, month, day);
                }
            } catch (Exception e) {
                return null;
            }
        }
        if (date instanceof Number) {
            try {
                long ms = ((Number) date).longValue();
                return Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    // Getters y setters
    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    @JsonProperty("due_date")
    public LocalDate getDue_date() { return due_date_field; }
    
    @JsonProperty("start_date")
    public LocalDate getStart_date() { return start_date_field; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getSubjectColor() { return subjectColor; }
    public void setSubjectColor(String subjectColor) { this.subjectColor = subjectColor; }
}
