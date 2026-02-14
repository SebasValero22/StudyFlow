package com.iescamp.studyflow.model;



import java.util.Date;
import lombok.Data;
@Data
public class Subject {

    private int subjectId;
    private int userId;
    private String nombre;
    private String color;
    private boolean active;
    private Date beginning_date;
    private Date due_date;

}
