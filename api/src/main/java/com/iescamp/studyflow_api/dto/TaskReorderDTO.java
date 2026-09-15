package com.iescamp.studyflow_api.dto;

import lombok.Data;

@Data
public class TaskReorderDTO {
    private Integer taskId;
    private Integer orderIndex;
}
