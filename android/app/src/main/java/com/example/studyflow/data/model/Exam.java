package com.example.studyflow.data.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Exam {

    private Integer examId;

    private Subject subject;


    @JsonProperty("nameExam")
    private String examName;


    private String examType;


    private LocalDate examDate;


    private String classroom;

    public Exam(){}
    public Exam(Integer examId, Subject subject, String name, String examType, LocalDate examDate, String classroom) {
        this.examId = examId;
        this.subject = subject;
        this.examName = name;
        this.examType = examType;
        this.examDate = examDate;
        this.classroom = classroom;
    }

    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getName() {
        return examName;
    }

    public void setName(String name) {
        this.examName = name;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }
}
