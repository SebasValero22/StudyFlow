package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface ExamRepository extends JpaRepository<Exam, Integer> {

    List<Exam> findBySubject_NameSubject(String nameSubject);
}