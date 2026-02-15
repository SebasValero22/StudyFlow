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
    private String title;
    private String description;
    private Priority priority;
    private boolean isCompleted;
    private Integer subjectId;

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
        } else if (date instanceof String) {
            return LocalDate.parse((String) date);
        }
        return null;
    }

    public static TaskResponseDTO convertToDTO(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setTaskId(task.getTaskId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriority());
        dto.setStart_date(task.getStart_date());
        dto.setDue_date(task.getDue_date());
        dto.setCompleted(task.getIsCompleted());

        if (task.getSubjectId() != null) {
            dto.setSubjectId(task.getSubjectId().getSubjectId());
        }
        return dto;
    }
}