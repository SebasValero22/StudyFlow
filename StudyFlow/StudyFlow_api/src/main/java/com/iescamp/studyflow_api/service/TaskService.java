package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.dto.TaskResponseDTO;
import com.iescamp.studyflow_api.model.Task;
import com.iescamp.studyflow_api.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public TaskResponseDTO add(TaskResponseDTO dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setStart_date(dto.getStart_date());
        task.setDue_date(dto.getDue_date());
        task.setIsCompleted(dto.isCompleted());
        return TaskResponseDTO.convertToDTO(taskRepository.save(task));
    }

    public TaskResponseDTO modify(Integer id, TaskResponseDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setDue_date(dto.getDue_date());
        task.setIsCompleted(dto.isCompleted());
        return TaskResponseDTO.convertToDTO(taskRepository.save(task));
    }

    public void delete(Integer id) {
        if (!taskRepository.existsById(id)) throw new RuntimeException("Task not found");
        taskRepository.deleteById(id);
    }
}
