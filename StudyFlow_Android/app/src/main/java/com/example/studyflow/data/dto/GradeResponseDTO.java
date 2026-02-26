package com.example.studyflow.data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GradeResponseDTO {
    private Integer gradeId;
    private String concept;
    private Double score;
    private Double weight;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate gradeDate;

    public Integer getGradeId() { return gradeId; }
    public void setGradeId(Integer gradeId) { this.gradeId = gradeId; }
    public String getConcept() { return concept; }
    public void setConcept(String concept) { this.concept = concept; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public LocalDate getGradeDate() { return gradeDate; }
    public void setGradeDate(LocalDate gradeDate) { this.gradeDate = gradeDate; }
}
