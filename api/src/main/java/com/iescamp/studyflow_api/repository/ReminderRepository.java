package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Integer> {
    // Find reminders for a specific task
    List<Reminder> findByTask_TaskId(Integer taskId);

    // Find reminders for a specific exam
    List<Reminder> findByExam_ExamId(Integer examId);
}
