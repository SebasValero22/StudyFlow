package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.ExamDTO;
import com.iescamp.studyflow_api.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    @Autowired
    private ExamService examService;

    // Faltaba el @RequestBody
    @PostMapping
    public ExamDTO add(@RequestBody ExamDTO dto){
        return examService.add(dto);
    }

    @PutMapping("/{id}")
    public ExamDTO update(@PathVariable Integer id, @RequestBody ExamDTO dto) {
        return examService.modify(id, dto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        examService.delete_(id);
        return "Exam deleted successfully";
    }

    //devuelve List<ExamDTO>
    @GetMapping("/name/{name}")
    public List<ExamDTO> findByName(@PathVariable String name) {
        return examService.findByName(name);
    }

    // Ahora devuelve List<ExamDTO>
    @GetMapping("/subject/{subject}")
    public List<ExamDTO> findBySubject(@PathVariable String subject) {
        return examService.findBySubject(subject);
    }

    @GetMapping
    public List<ExamDTO> getAll() {
        return examService.findAll();
    }
}