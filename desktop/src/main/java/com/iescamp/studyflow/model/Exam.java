package com.iescamp.studyflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Exam {
    private Integer examId;

    @JsonProperty("lifeAreaId")
    private Integer subjectId;

    private String nameExam;
    private String examType;
    private LocalDate examDate;
    private String classroom;

    @JsonProperty("lifeAreaName")
    private String subjectName;

    @JsonProperty("lifeAreaColor")
    private String subjectColor;

    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String syncStatus;
    private String clientUuid;
}
