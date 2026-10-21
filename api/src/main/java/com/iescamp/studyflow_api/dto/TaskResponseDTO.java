package com.iescamp.studyflow_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.iescamp.studyflow_api.model.Task;
import com.iescamp.studyflow_api.model.enums.Priority;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Data
public class TaskResponseDTO {
    private Integer taskId;
    private Integer userId;
    private String title;
    private String description;
    private Priority priority;

    // Canonical wire name. Hand-written so Lombok doesn't ALSO derive "completed".
    @JsonProperty("isCompleted")
    private boolean isCompleted;

    @JsonProperty("isCompleted")
    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }
    private Integer orderIndex;
    private Integer lifeAreaId;
    private String lifeAreaName;
    private String lifeAreaColor;
    private Integer projectId;

    // Campos de fecha que aceptarán los milisegundos
    private LocalDate start_date;
    private LocalDate due_date;

    // Sobrescribimos los setters para manejar los milisegundos si llegan como números
    @JsonProperty("start_date")
    public void setStart_date(Object date) {
        this.start_date = parseDate(date);
    }

    @JsonProperty("due_date")
    public void setDue_date(Object date) {
        this.due_date = parseDate(date);
    }

    private LocalDate parseDate(Object date) {
        if (date instanceof Long) {
            return Instant.ofEpochMilli((Long) date).atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (date instanceof LocalDate d) {
            return d;
        } else if (date instanceof String) {
            return LocalDate.parse((String) date);
        }
        return null;
    }

    public static TaskResponseDTO convertToDTO(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setTaskId(task.getTaskId());
        dto.setUserId(task.getUserId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriority());
        dto.setStart_date(task.getStart_date());
        dto.setDue_date(task.getDue_date());
        dto.setCompleted(task.getIsCompleted());
        dto.setOrderIndex(task.getOrderIndex());

        if (task.getLifeArea() != null) {
            dto.setLifeAreaId(task.getLifeArea().getLifeAreaId());
            dto.setLifeAreaName(task.getLifeArea().getName());
            dto.setLifeAreaColor(task.getLifeArea().getColor());
        }
        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getProjectId());
        }
        return dto;
    }
}