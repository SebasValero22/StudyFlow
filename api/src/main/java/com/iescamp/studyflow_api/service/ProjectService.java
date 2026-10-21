package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.model.Project;
import com.iescamp.studyflow_api.model.ProjectMember;
import com.iescamp.studyflow_api.model.User;
import com.iescamp.studyflow_api.repository.LifeAreaRepository;
import com.iescamp.studyflow_api.repository.ProjectMemberRepository;
import com.iescamp.studyflow_api.repository.ProjectRepository;
import com.iescamp.studyflow_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final LifeAreaRepository lifeAreaRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository memberRepository,
                          LifeAreaRepository lifeAreaRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.lifeAreaRepository = lifeAreaRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Project create(Integer userId, Integer lifeAreaId, String name) {
        var area = lifeAreaRepository.findById(lifeAreaId)
                .filter(a -> a.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "LifeArea not found"));

        Project project = new Project();
        project.setLifeArea(area);
        project.setName(name);
        project.setActive(true);
        Project saved = projectRepository.save(project);

        ProjectMember owner = new ProjectMember();
        owner.setProjectId(saved.getProjectId());
        owner.setUserId(userId);
        owner.setRole(ProjectMember.Role.OWNER);
        memberRepository.save(owner);
        return saved;
    }

    /** Projects the user owns or is a member of. */
    public List<Project> listForUser(Integer userId) {
        List<Integer> sharedIds = memberRepository.findByUserId(userId).stream()
                .map(ProjectMember::getProjectId).toList();
        if (sharedIds.isEmpty()) {
            return List.of();
        }
        return projectRepository.findAllById(sharedIds);
    }

    @Transactional
    public void delete(Integer userId, Integer projectId) {
        requireRole(userId, projectId, ProjectMember.Role.OWNER);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        project.setDeletedAt(now);
        project.setUpdatedAt(now);
        projectRepository.save(project);
        // Members have no deletedAt; drop them so canRead/canWrite stop matching the tombstone.
        memberRepository.findByProjectId(projectId).forEach(memberRepository::delete);
    }

    public record MemberView(Integer memberId, Integer userId, String email, String name, String role) {}

    public List<MemberView> members(Integer userId, Integer projectId) {
        requireMembership(userId, projectId);
        return memberRepository.findByProjectId(projectId).stream()
                .map(m -> {
                    User u = userRepository.findById(m.getUserId()).orElse(null);
                    return new MemberView(m.getMemberId(), m.getUserId(),
                            u != null ? u.getEmail() : "?", u != null ? u.getUserName() : "?",
                            m.getRole().name());
                })
                .toList();
    }

    @Transactional
    public MemberView share(Integer ownerId, Integer projectId, String email, String roleStr) {
        requireRole(ownerId, projectId, ProjectMember.Role.OWNER);

        ProjectMember.Role role = parseRole(roleStr);
        if (role == ProjectMember.Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add another OWNER");
        }
        User invitee = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No user with that email"));

        memberRepository.findByProjectIdAndUserId(projectId, invitee.getUserId())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "User already a member");
                });

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(invitee.getUserId());
        member.setRole(role);
        memberRepository.save(member);
        return new MemberView(member.getMemberId(), invitee.getUserId(), invitee.getEmail(),
                invitee.getUserName(), role.name());
    }

    public void removeMember(Integer callerId, Integer projectId, Integer memberId) {
        ProjectMember member = memberRepository.findById(memberId)
                .filter(m -> m.getProjectId().equals(projectId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        // Owners can remove anyone; members can only remove themselves (leave).
        if (!member.getUserId().equals(callerId)) {
            requireRole(callerId, projectId, ProjectMember.Role.OWNER);
        } else if (member.getRole() == ProjectMember.Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Owners cannot leave their own project; transfer or delete it instead");
        }
        memberRepository.delete(member);
    }

    // ---- access helpers used by TaskService ----

    public boolean canRead(Integer userId, Integer projectId) {
        return memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                projectId, userId, List.of(ProjectMember.Role.values()));
    }

    public boolean canWrite(Integer userId, Integer projectId) {
        return memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                projectId, userId, List.of(ProjectMember.Role.OWNER, ProjectMember.Role.EDITOR));
    }

    public List<Integer> readableProjectIds(Integer userId) {
        return memberRepository.findByUserId(userId).stream()
                .map(ProjectMember::getProjectId).toList();
    }

    public void requireRole(Integer userId, Integer projectId, ProjectMember.Role minimum) {
        ProjectMember member = memberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        int rank = switch (member.getRole()) {
            case VIEWER -> 0;
            case EDITOR -> 1;
            case OWNER -> 2;
        };
        int required = switch (minimum) {
            case VIEWER -> 0;
            case EDITOR -> 1;
            case OWNER -> 2;
        };
        if (rank < required) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient project role");
        }
    }

    private void requireMembership(Integer userId, Integer projectId) {
        requireRole(userId, projectId, ProjectMember.Role.VIEWER);
    }

    private static ProjectMember.Role parseRole(String role) {
        try {
            return ProjectMember.Role.valueOf(role == null ? "" : role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role must be EDITOR or VIEWER");
        }
    }
}
