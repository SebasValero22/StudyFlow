package com.iescamp.studyflow_api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "users") // Asegura que coincide con tu tabla SQL
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId; // Coincide con userId del SQL

    @Column(name = "userName", nullable = false)
    private String userName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "passwordHash", nullable = false)
    private String password;

    @Column(name = "registrationDate")
    private Date registrationDate;

    @Column(name = "activeUser")
    private Boolean active;
}