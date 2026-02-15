package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.SubjectResponseDTO;
import com.iescamp.studyflow_api.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
// @CrossOrigin(origins = "*") // Descomenta si tienes problemas de conexión desde otro PC
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    // CREATE: Add a new subject
    @PostMapping
    public ResponseEntity<?> add(@RequestBody SubjectResponseDTO addSubject) {
        try {
            System.out.println("Recibiendo asignatura: " + addSubject.getNameSubject()); // DEBUG

            // Parche de seguridad: Si no viene userId, poner 1
            if (addSubject.getUserId() == null) {
                addSubject.setUserId(1);
            }

            SubjectResponseDTO saved = subjectService.add(addSubject);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace(); // ¡ESTO ES LO IMPORTANTE! Verás el error real en la consola API
            return ResponseEntity.status(500).body("Error en el servidor: " + e.getMessage());
        }
    }

    // UPDATE: Modify an existing subject by its ID
    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> update(@PathVariable Integer id, @RequestBody SubjectResponseDTO data) {
        SubjectResponseDTO updated = subjectService.modify(id, data);
        return ResponseEntity.ok(updated);
    }

    // DELETE: Remove a subject by its ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        subjectService.delete(id);
        return ResponseEntity.ok("Subject with ID " + id + " deleted successfully");
    }

    // READ ALL: Get a list of all subjects
    @GetMapping
    public List<SubjectResponseDTO> findAll() {
        return subjectService.findAll();
    }

    // READ ONE: Get a specific subject by its ID
    @GetMapping("/{id}")
    public SubjectResponseDTO findById(@PathVariable Integer id) {
        return subjectService.findById(id);
    }

    // SEARCH: Get all subjects that match a specific name
    @GetMapping("/search")
    public List<SubjectResponseDTO> findByName(@RequestParam String name) {
        return subjectService.findByName(name);
    }
}