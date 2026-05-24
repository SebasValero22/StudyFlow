package com.iescamp.studyflow.model;

import Enums.TypeOfReminder;

import java.util.Date;
import lombok.Data;
@Data
public class Reminder {

    private int reminderId;
    private int subjectId;
    private TypeOfReminder typeOfReminder;
    private Date date;

}
