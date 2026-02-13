package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface ExamRepository extends JpaRepository<Exam, Integer> {

    // Este está perfecto porque Exam tiene una variable "name"
    List<Exam> findByName(String name);

    // ¡EL ARREGLO! Entra en Subject y busca por "name"
    List<Exam> findBySubject_Name(String subjectName);
}