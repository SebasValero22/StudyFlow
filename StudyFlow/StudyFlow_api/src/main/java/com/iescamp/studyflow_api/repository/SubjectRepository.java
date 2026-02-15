package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    List<Subject> findByNameSubject(String nameSubject);}