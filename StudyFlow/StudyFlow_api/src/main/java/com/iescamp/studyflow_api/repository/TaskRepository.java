package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.Task;
import com.iescamp.studyflow_api.model.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository  extends JpaRepository<Task,Integer> {
    List<Task> findBySubjectId(Integer subjectId);
    List<Task> findByPriority(Priority priority);}
