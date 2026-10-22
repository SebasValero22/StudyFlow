package com.iescamp.studyflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {
    private Integer taskId;

    @JsonProperty("lifeAreaId")
    private Integer subjectId;

    private String title;

    @JsonProperty("description")
    private String descriptionTask;

    @JsonProperty("due_date")
    private LocalDate dueDate;

    @JsonProperty("startDate")
    private LocalDate startDate;

    private String priority;

    @JsonProperty("isCompleted")
    private Boolean isCompleted = false;

    private Boolean isHabit = false;

    private Integer orderIndex = 0;

    private Integer projectId;

    @JsonProperty("lifeAreaName")
    private String subjectName;

    @JsonProperty("lifeAreaColor")
    private String subjectColor;

    // Implementation Intention fields (RAWFOCUS)
    private String trigger;
    private String microStep;
    private String energyBucket; // "morning", "afternoon", "evening"
    private Integer duration; // minutes

    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String syncStatus;
    private String clientUuid;
}