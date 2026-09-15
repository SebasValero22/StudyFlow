package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
}
