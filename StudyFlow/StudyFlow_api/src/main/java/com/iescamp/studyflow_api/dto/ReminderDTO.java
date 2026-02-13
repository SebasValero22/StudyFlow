package com.iescamp.studyflow_api.dto;

import com.iescamp.studyflow_api.model.Exam;
import com.iescamp.studyflow_api.model.Reminder;
import com.iescamp.studyflow_api.model.Task;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
@Data
public class ReminderDTO {

    private Integer reminderId;
    private String message;
    private Date dateTime;
    private Task task;
    private Exam exam;



    public static ReminderDTO convertToDTO(Reminder reminder){
        ReminderDTO dto = new ReminderDTO();
        dto.setReminderId(reminder.getReminderId());
        dto.setTask(reminder.getTask());
        dto.setExam(reminder.getExam());
        dto.setMessage(reminder.getMessage());
        dto.setDateTime(reminder.getDateTime());
        return dto;
    }
}
