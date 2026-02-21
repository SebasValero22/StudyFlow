package com.iescamp.studyflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Grade {
    private int gradeId;
    private Integer subjectId;
    private String concept;
    private double score;
    private double weight;
    private String gradeDate;
}