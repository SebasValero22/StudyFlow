package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.ExamDTO;
import com.iescamp.studyflow_api.dto.FileDTO;
import com.iescamp.studyflow_api.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    // POST a /api/files (Se usa @RequestBody para leer el JSON)
    @PostMapping
    public FileDTO add(@RequestBody FileDTO dto){
        return fileService.add(dto);
    }

    // DELETE a /api/files/5 (Una sola llave, y @PathVariable)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id){
        fileService.delete(id);
    }

    // PUT es el estándar para actualizar, no POST
    @PutMapping("/{id}")
    public FileDTO update(@PathVariable Integer id, @RequestBody FileDTO dto){
        return fileService.modify(id, dto);
    }

    // GET a /api/files (Devuelve una LISTA)
    @GetMapping
    public List<FileDTO> findAll(){
        return fileService.findAll(); // Asegúrate de que tu servicio devuelve List<FileDTO>
    }

    // GET a /api/files/type/pdf (Añadido /type/ para que no choque con otras rutas)
    @GetMapping("/type/{type}")
    public List<FileDTO> findByType(@PathVariable String type){
        return fileService.findByType(type); // Arreglado el typo "retunr" y pasado el parámetro
    }

}