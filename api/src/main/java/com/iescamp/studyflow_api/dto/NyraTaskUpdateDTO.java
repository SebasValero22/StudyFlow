package com.iescamp.studyflow_api.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class NyraTaskUpdateDTO {
    private Integer taskId;
    private LocalDate suggestedDueDate;
    private String priority; // HIGH, MEDIUM, LOW
    private Integer orderIndex;
}
