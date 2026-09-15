package com.iescamp.studyflow_api.model;

import com.iescamp.studyflow_api.model.enums.Priority;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity 
@Table(name = "Tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taskId")
    private Integer taskId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lifeAreaId", nullable = true)
    private LifeArea lifeArea;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "projectId", nullable = true)
    private Project project;

    @Column(nullable = false)
    private String title;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "descriptionTask")
    private String description;

    private LocalDate start_date;
    private LocalDate due_date;

    @Column(name = "isCompleted")
    private Boolean completed = false;

    @Column(name = "isHabit")
    private Boolean isHabit = false;

    @Column(name = "orderIndex")
    private Integer orderIndex = 0;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    public Boolean getIsCompleted() {
        return completed;
    }
}