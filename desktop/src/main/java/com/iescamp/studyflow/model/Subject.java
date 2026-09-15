package com.iescamp.studyflow.model;

import lombok.Data;
@Data
public class Subject {
    private int subjectId;
    private int userId;
    private String nameSubject;
    private String color;
    private boolean activeSubject;
    private String academicYear;
}