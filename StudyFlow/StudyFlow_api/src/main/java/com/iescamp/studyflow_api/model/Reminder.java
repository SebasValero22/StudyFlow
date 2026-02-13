package com.iescamp.studyflow_api.model;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "Reminders")
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reminderId;

    private String message;

    @Column(name = "date_Time")
    private java.util.Date dateTime;

    @ManyToOne
    @JoinColumn(name = "taskId", nullable = true) // SQL allows NULL
    private Task task;

    @ManyToOne
    @JoinColumn(name = "examId", nullable = true) // SQL allows NULL
    private Exam exam;
}
