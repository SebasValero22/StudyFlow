package com.iescamp.studyflow_api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate; // Better than java.util.Date

/**
 * Mapped to 'users' table.
 * Represents a registered student and owner of {@link Subject} and {@link Task} entries.
 */



@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId; // We merged 'id' and 'userId' into one. You only need one Primary Key.

    @Column(nullable = false) // Name cannot be empty
    private String name;

    @Column(unique = true, nullable = false) // Email must be unique in the database
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "registrationDate")
    private LocalDate registrationDate = LocalDate.now(); // Sets today's date automatically

    private boolean active = true; // Default to true
}