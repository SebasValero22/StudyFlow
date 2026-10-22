package com.iescamp.studyflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Reminder {
    private Integer reminderId;
    private String message;
    private LocalDateTime dateTime;
    private Integer taskId;
    private Integer examId;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String syncStatus;
    private String clientUuid;
}
