package com.example.studyflow.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubjectResponseDTO {
    private Integer subjectId;
    private String nameSubject;
    private String color;

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public String getNameSubject() { return nameSubject; }
    public void setNameSubject(String nameSubject) { this.nameSubject = nameSubject; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
