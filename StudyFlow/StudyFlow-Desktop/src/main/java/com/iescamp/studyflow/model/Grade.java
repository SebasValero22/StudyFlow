package com.iescamp.studyflow.model;
import lombok.Data;
@Data
public class Grade {


    private int gradeId;
    private int subjectId;
    private String concept;
    private double grade;
    private double weight;

}
