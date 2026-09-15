package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.model.Project;
import com.iescamp.studyflow_api.security.CurrentUserProvider;
import com.iescamp.studyflow_api.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CurrentUserProvider currentUser;

    public record CreateRequest(Integer lifeAreaId, String name) {}
    public record ShareRequest(String email, String role) {}

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateRequest request) {
        try {
            Project saved = projectService.create(
                    currentUser.getUserId(), request.lifeAreaId(), request.name());
            return ResponseEntity.ok(saved);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Could not create project");
        }
    }

    /** Projects you own or are a member of. */
    @GetMapping
    public List<Project> list() {
        return projectService.listForUser(currentUser.getUserId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            projectService.delete(currentUser.getUserId(), id);
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (ResponseStatusException rse) {
            throw rse;
        }
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<?> members(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(projectService.members(currentUser.getUserId(), id));
        } catch (ResponseStatusException rse) {
            throw rse;
        }
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> share(@PathVariable Integer id, @RequestBody ShareRequest request) {
        try {
            return ResponseEntity.ok(projectService.share(
                    currentUser.getUserId(), id, request.email(), request.role()));
        } catch (ResponseStatusException rse) {
            throw rse;
        }
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable Integer id, @PathVariable Integer memberId) {
        try {
            projectService.removeMember(currentUser.getUserId(), id, memberId);
            return ResponseEntity.ok(Map.of("removed", true));
        } catch (ResponseStatusException rse) {
            throw rse;
        }
    }
}
