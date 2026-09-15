package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {
    List<ProjectMember> findByUserId(Integer userId);
    List<ProjectMember> findByProjectId(Integer projectId);
    Optional<ProjectMember> findByProjectIdAndUserId(Integer projectId, Integer userId);
    boolean existsByProjectIdAndUserIdAndRoleIn(Integer projectId, Integer userId, List<ProjectMember.Role> roles);
}
