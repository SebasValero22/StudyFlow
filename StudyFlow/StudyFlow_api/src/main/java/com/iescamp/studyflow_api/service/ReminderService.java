package com.iescamp.studyflow_api.service;


import com.iescamp.studyflow_api.dto.ReminderDTO;
import com.iescamp.studyflow_api.model.Reminder;
import com.iescamp.studyflow_api.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    public ReminderDTO add(ReminderDTO dto){
        Reminder reminder = new Reminder();
        reminder.setMessage(dto.getMessage());
        reminder.setDateTime(dto.getDateTime());
        // link these if they exist in your DTO
        reminder.setTask(dto.getTask());
        reminder.setExam(dto.getExam());
        return ReminderDTO.convertToDTO(reminderRepository.save(reminder));
    }

    public void delete(Integer id){
        if (!reminderRepository.existsById(id)) throw new RuntimeException("Reminder not found");
        reminderRepository.deleteById(id);
        // Remove the 'throw new RuntimeException' success message!
    }
    public ReminderDTO update(Integer id, ReminderDTO dto){
        Reminder reminder = reminderRepository.findById(id).orElseThrow();
        reminder.setMessage(dto.getMessage());
        reminder.setDateTime(dto.getDateTime());
        return ReminderDTO.convertToDTO(reminderRepository.save(reminder));
    }
    public List<ReminderDTO> findAll(){
        return reminderRepository.findAll().stream().map(ReminderDTO::convertToDTO).toList();
    }


}
