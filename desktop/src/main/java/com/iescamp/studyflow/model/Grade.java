package com.iescamp.studyflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Grade {
    private Integer gradeId;
    private Integer subjectId;
    private String concept;
    private Double score;
    private Double weight;
    private String gradeDate;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String syncStatus;
    private String clientUuid;
}
