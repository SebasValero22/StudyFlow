package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.GradeDTO;
import com.iescamp.studyflow_api.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/grades")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    // POST /api/grades (crear)
    @PostMapping
    public GradeDTO add(@RequestBody GradeDTO dto){
        return gradeService.add(dto);
    }

    // GET /api/grades (listar)
    @GetMapping
    public List<GradeDTO> findAll(@RequestParam(required = false) Integer userId){
        return gradeService.findAll(userId);
    }

    // PUT /api/grades/5 (actualizar)
    @PutMapping("/{id}")
    public GradeDTO update(@PathVariable Integer id, @RequestBody GradeDTO dto) {
        return gradeService.modify(id, dto);
    }

    // DELETE /api/grades/5 (borrar)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        gradeService.delete(id);
    }
}
