package com.iescamp.studyflow_api.model;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "grades")
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer gradeId;
    @ManyToOne
    @JoinColumn(name = "subjectId")
    private Subject subject;
    private String concept;
    private Double score;
    private Double weight;
    private Date date;

}
