package com.iescamp.studyflow.model;

import Enums.ExamType;

import java.util.Date;
import lombok.Data;
@Data
public class Exam {
    private int examID;
    private int subjectID;
    private String name;
    private ExamType type;
    private Date examDate;




}
