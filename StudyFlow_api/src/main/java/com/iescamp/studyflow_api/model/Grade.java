package com.iescamp.studyflow_api.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
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
    @Column(name = "gradeDate")
    private LocalDate gradeDate;

}
