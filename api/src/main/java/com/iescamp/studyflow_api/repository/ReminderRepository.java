package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Integer> {
    // Recordatorios de una tarea concreta
    List<Reminder> findByTask_TaskId(Integer taskId);

    // Recordatorios de un examen concreto
    List<Reminder> findByExam_ExamId(Integer examId);
}
