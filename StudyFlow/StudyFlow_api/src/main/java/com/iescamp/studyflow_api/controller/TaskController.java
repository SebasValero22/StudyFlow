package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.TaskResponseDTO;
import com.iescamp.studyflow_api.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // CREATE: Add a new task
    @PostMapping
    public ResponseEntity<?> add(@RequestBody TaskResponseDTO dto) {
        try {
            TaskResponseDTO saved = taskService.add(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace(); // Esto nos mostrará si falla la base de datos después del parseo
            return ResponseEntity.badRequest().body("Error al procesar la tarea: " + e.getMessage());
        }
    }

    // UPDATE: Modify an existing task (EL PUT QUE PEDISTE)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody TaskResponseDTO dto) {
        try {
            System.out.println("DEBUG: Actualizando tarea ID: " + id);
            TaskResponseDTO updated = taskService.modify(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace(); // Ver error real en consola API
            return ResponseEntity.status(500).body("Error al actualizar tarea: " + e.getMessage());
        }
    }

    // DELETE: Remove a task
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        try {
            taskService.delete(id);
            return ResponseEntity.ok("Task deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al borrar: " + e.getMessage());
        }
    }

    // READ ALL: Obtener todas las tareas
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> findAll() {
        return ResponseEntity.ok(taskService.findAll());
    }

    // READ ONE: Obtener una tarea por ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

}