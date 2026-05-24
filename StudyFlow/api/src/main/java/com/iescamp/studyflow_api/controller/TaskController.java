package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.TaskResponseDTO;
import com.iescamp.studyflow_api.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*") // Necesario para la PWA
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Crear una tarea
    @PostMapping
    public ResponseEntity<?> add(@RequestBody TaskResponseDTO dto) {
        try {
            TaskResponseDTO saved = taskService.add(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace(); // Para ver si falla la base de datos despues del parseo
            return ResponseEntity.badRequest().body("Error al procesar la tarea: " + e.getMessage());
        }
    }

    // Actualizar una tarea existente
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody TaskResponseDTO dto) {
        try {
            System.out.println("DEBUG: Actualizando tarea ID: " + id);
            TaskResponseDTO updated = taskService.modify(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error real en consola
            return ResponseEntity.status(500).body("Error al actualizar tarea: " + e.getMessage());
        }
    }

    // Borrar una tarea
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        try {
            taskService.delete(id);
            return ResponseEntity.ok("Task deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al borrar: " + e.getMessage());
        }
    }

    // Obtener todas las tareas
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> findAll(@RequestParam(required = false) Integer userId) {
        return ResponseEntity.ok(taskService.findAll(userId));
    }

    // Obtener una tarea por id
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

}
