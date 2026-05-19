package com.example.studyflow.data.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Subject {

    private Integer subjectId;
    private Integer userId;
    private String nameSubject;
    private String academicYear;
    private String color;
    private Boolean activeSubject;

    public Subject(){}
    public Subject(Integer subjectId, Integer userId, String nameSubject, String academicYear, String color, Boolean activeSubject) {
        this.subjectId = subjectId;
        this.userId = userId;
        this.nameSubject = nameSubject;
        this.academicYear = academicYear;
        this.color = color;
        this.activeSubject = activeSubject;
    }

    public String getNameSubject() {
        return nameSubject;
    }

    public void setNameSubject(String nameSubject) {
        this.nameSubject = nameSubject;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getActiveSubject() {
        return activeSubject;
    }

    public void setActiveSubject(Boolean activeSubject) {
        this.activeSubject = activeSubject;
    }
}
