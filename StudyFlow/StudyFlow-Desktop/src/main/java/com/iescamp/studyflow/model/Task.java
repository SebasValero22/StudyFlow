package com.iescamp.studyflow.model;
import com.iescamp.studyflow.enums.Priority;

import java.time.LocalDate;
import java.util.Date;

import lombok.Data;
@Data
public class Task {

    private Integer taskId;
    private Subject subjectId;
    private String title;
    private String description;
    private LocalDate start_date;
    private LocalDate due_date;
    private String isCompleted;
    private Priority priority;





}
