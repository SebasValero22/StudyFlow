package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.SubjectResponseDTO;
import com.iescamp.studyflow_api.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    // Crear una asignatura
    @PostMapping
    public ResponseEntity<?> add(@RequestBody SubjectResponseDTO addSubject) {
        try {
            System.out.println("Recibiendo asignatura: " + addSubject.getNameSubject());

            if (addSubject.getUserId() == null) {
                return ResponseEntity.badRequest().body("Error: userId is required");
            }

            SubjectResponseDTO saved = subjectService.add(addSubject);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error real en consola
            return ResponseEntity.status(500).body("Error en el servidor: " + e.getMessage());
        }
    }

    // Actualizar una asignatura por id
    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> update(@PathVariable Integer id, @RequestBody SubjectResponseDTO data) {
        SubjectResponseDTO updated = subjectService.modify(id, data);
        return ResponseEntity.ok(updated);
    }

    // Borrar una asignatura por id
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        subjectService.delete(id);
        return ResponseEntity.ok("Subject with ID " + id + " deleted successfully");
    }

    // Obtener asignaturas (opcionalmente filtradas por userId)
    @GetMapping
    public List<SubjectResponseDTO> findAll(@RequestParam(required = false) Integer userId) {
        return subjectService.findAll(userId);
    }

    // Obtener una asignatura por id
    @GetMapping("/{id}")
    public SubjectResponseDTO findById(@PathVariable Integer id) {
        return subjectService.findById(id);
    }

    // Buscar asignaturas por nombre
    @GetMapping("/search")
    public List<SubjectResponseDTO> findByName(@RequestParam String name) {
        return subjectService.findByName(name);
    }
}
