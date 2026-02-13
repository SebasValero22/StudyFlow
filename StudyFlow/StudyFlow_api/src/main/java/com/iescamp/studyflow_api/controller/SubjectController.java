package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.SubjectResponseDTO;
import com.iescamp.studyflow_api.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    // CREATE: Add a new subject
    @PostMapping
    public SubjectResponseDTO add(@RequestBody SubjectResponseDTO addSubject) {
        return subjectService.add(addSubject);
    }

    // UPDATE: Modify an existing subject by its ID
    @PutMapping("/{id}")
    public SubjectResponseDTO update(@PathVariable Integer id, @RequestBody SubjectResponseDTO data) {
        return subjectService.modify(id, data);
    }

    // DELETE: Remove a subject by its ID
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        subjectService.delete(id);
        return "Subject with ID " + id + " deleted successfully";
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
    // Usage: /api/subjects/search?name=Math
    @GetMapping("/search")
    public List<SubjectResponseDTO> findByName(@RequestParam String name) {
        return subjectService.findByName(name);
    }
}