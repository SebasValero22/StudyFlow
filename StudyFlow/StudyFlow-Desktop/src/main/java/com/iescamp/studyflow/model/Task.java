package com.iescamp.studyflow.model;

import lombok.Data;
import java.util.Date;

@Data
public class Task {
    private int taskId;
    private Integer subjectId;
    private String title;
    private String description;
    private Date due_date;
    private String priority;
    private Boolean isCompleted;
}