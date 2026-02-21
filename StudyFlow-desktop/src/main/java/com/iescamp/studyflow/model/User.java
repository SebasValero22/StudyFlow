package com.iescamp.studyflow.model;

import java.util.Date;

import lombok.Data;
@Data
public class User {

    private int userId;
    private String name;
    private String email;
    private String password;
    private Date registration_date;
    private boolean active;

}
