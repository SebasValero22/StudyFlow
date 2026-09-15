package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.LifeAreaResponseDTO;
import com.iescamp.studyflow_api.security.CurrentUserProvider;
import com.iescamp.studyflow_api.service.LifeAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class LifeAreaController {

    @Autowired
    private LifeAreaService lifeAreaService;

    @Autowired
    private CurrentUserProvider currentUser;

    @PostMapping
    public ResponseEntity<?> add(@RequestBody LifeAreaResponseDTO addLifeArea) {
        try {
            return ResponseEntity.ok(lifeAreaService.add(currentUser.getUserId(), addLifeArea));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error en el servidor: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody LifeAreaResponseDTO data) {
        try {
            return ResponseEntity.ok(lifeAreaService.modify(currentUser.getUserId(), id, data));
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al actualizar: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            lifeAreaService.delete(currentUser.getUserId(), id);
            return ResponseEntity.ok("Deleted successfully");
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al borrar: " + e.getMessage());
        }
    }

    @GetMapping
    public List<LifeAreaResponseDTO> findAll() {
        return lifeAreaService.findAll(currentUser.getUserId());
    }

    @GetMapping("/{id}")
    public LifeAreaResponseDTO findById(@PathVariable Integer id) {
        return lifeAreaService.findById(currentUser.getUserId(), id);
    }

    @GetMapping("/search")
    public List<LifeAreaResponseDTO> findByName(@RequestParam String name) {
        return lifeAreaService.findByName(currentUser.getUserId(), name);
    }
}
