package com.iescamp.studyflow_api.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class NyraScheduleResponseDTO {
    private Integer taskId;
    private LocalDate suggestedDueDate;
    private Integer orderIndex;
    private String suggestedLifeArea;
}
