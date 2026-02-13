package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.TaskResponseDTO;
import com.iescamp.studyflow_api.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
        @Autowired
        private TaskService taskService;

        @PostMapping
        public TaskResponseDTO add(@RequestBody TaskResponseDTO dto) {
            return taskService.add(dto);
        }

        @PutMapping("/{id}")
        public TaskResponseDTO update(@PathVariable Integer id, @RequestBody TaskResponseDTO dto) {
            return taskService.modify(id, dto);
        }

        @DeleteMapping("/{id}")
        public String delete(@PathVariable Integer id) {
            taskService.delete(id);
            return "Task deleted successfully";
        }
}
