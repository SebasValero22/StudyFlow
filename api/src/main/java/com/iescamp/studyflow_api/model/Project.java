package com.iescamp.studyflow_api.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "projectId")
    private Integer projectId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lifeAreaId", nullable = false)
    private LifeArea lifeArea;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(name = "active")
    private Boolean active;
}
