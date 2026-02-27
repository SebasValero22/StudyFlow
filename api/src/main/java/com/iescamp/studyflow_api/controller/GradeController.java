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

    // POST a /api/grades (Crear) - Añadido @RequestBody y quitado el /add
    @PostMapping
    public GradeDTO add(@RequestBody GradeDTO dto){
        return gradeService.add(dto); // Arreglado el "1" que tenías colado al final
    }

    // GET a /api/grades (Listar todos)
    @GetMapping
    public List<GradeDTO> findAll() {
        return gradeService.findAll();
    }

    // PUT a /api/grades/5 (Actualizar)
    @PutMapping("/{id}")
    public GradeDTO update(@PathVariable Integer id, @RequestBody GradeDTO dto) {
        return gradeService.modify(id, dto);
    }

    // DELETE a /api/grades/5 (Borrar)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        gradeService.delete(id);
    }
}