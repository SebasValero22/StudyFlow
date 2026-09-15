package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.TaskReorderDTO;
import com.iescamp.studyflow_api.dto.TaskResponseDTO;
import com.iescamp.studyflow_api.security.CurrentUserProvider;
import com.iescamp.studyflow_api.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private CurrentUserProvider currentUser;

    // CREATE: Add a new task (owner derived from JWT)
    @PostMapping
    public ResponseEntity<?> add(@RequestBody TaskResponseDTO dto) {
        try {
            return ResponseEntity.ok(taskService.add(currentUser.getUserId(), dto));
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al procesar la tarea: " + e.getMessage());
        }
    }

    // UPDATE: Modify an existing task
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody TaskResponseDTO dto) {
        try {
            return ResponseEntity.ok(taskService.modify(currentUser.getUserId(), id, dto));
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al actualizar tarea: " + e.getMessage());
        }
    }

    // DELETE: Remove a task
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        try {
            taskService.delete(currentUser.getUserId(), id);
            return ResponseEntity.ok("Task deleted successfully");
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al borrar: " + e.getMessage());
        }
    }

    // PATCH: Reorder tasks
    @PatchMapping("/reorder")
    public ResponseEntity<?> reorderTasks(@RequestBody List<TaskReorderDTO> reorderRequests) {
        try {
            taskService.reorderTasks(currentUser.getUserId(), reorderRequests);
            return ResponseEntity.ok("Tasks reordered successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al reordenar tareas: " + e.getMessage());
        }
    }

    // READ ALL: scoped to the authenticated user
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> findAll() {
        return ResponseEntity.ok(taskService.findAll(currentUser.getUserId()));
    }

    // READ INBOX: user's own undated, unprojected ideas
    @GetMapping("/inbox")
    public ResponseEntity<List<TaskResponseDTO>> findInboxTasks() {
        return ResponseEntity.ok(taskService.findInboxTasks(currentUser.getUserId()));
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> findById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(taskService.findById(currentUser.getUserId(), id));
        } catch (ResponseStatusException rse) {
            throw rse;
        }
    }
}
