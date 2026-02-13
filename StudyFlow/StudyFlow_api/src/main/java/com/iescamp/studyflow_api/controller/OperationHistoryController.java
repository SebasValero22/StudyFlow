package com.iescamp.studyflow_api.controller;


import com.iescamp.studyflow_api.dto.OperationHistoryDTO;
import com.iescamp.studyflow_api.service.OperationHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class OperationHistoryController {

    @Autowired
    private OperationHistoryService service;

    @PostMapping
    public OperationHistoryDTO create(@RequestBody OperationHistoryDTO dto) {
        return service.add(dto);
    }

    @PutMapping("/{id}")
    public OperationHistoryDTO update(@PathVariable Integer id, @RequestBody OperationHistoryDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "History record deleted";
    }

    @GetMapping
    public List<OperationHistoryDTO> getAll() {
        return service.findAll();
    }
}