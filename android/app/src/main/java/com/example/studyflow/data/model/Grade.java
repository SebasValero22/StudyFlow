package com.example.studyflow.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Grade {

    private Integer gradeId;

    private Subject subject;
    private String concept;
    private Double score;
    private Double weight;
    @JsonProperty("gradeDate")
    private LocalDate gradeDate;

    public Grade() {}
    public Grade(Integer gradeId, Subject subject, String concept, Double score, Double weight, LocalDate gradeDate) {
        this.gradeId = gradeId;
        this.subject = subject;
        this.concept = concept;
        this.score = score;
        this.weight = weight;
        this.gradeDate = gradeDate;
    }

    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public LocalDate getGradeDate() {
        return gradeDate;
    }

    public void setGradeDate(LocalDate gradeDate) {
        this.gradeDate = gradeDate;
    }
}
