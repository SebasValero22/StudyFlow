package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade,Integer> {
    List<Grade> findBySubject_UserId(Integer userId);
}

