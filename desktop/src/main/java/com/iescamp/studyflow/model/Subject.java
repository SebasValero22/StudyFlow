package com.iescamp.studyflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subject {
    @JsonProperty("lifeAreaId")
    private int subjectId;

    @JsonProperty("userId")
    private int userId;

    @JsonProperty("name")
    private String nameSubject;

    private String color;

    @JsonProperty("active")
    private boolean activeSubject;

    private String academicYear;
}
