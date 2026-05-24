package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.dto.TaskResponseDTO;
import com.iescamp.studyflow_api.model.Subject;
import com.iescamp.studyflow_api.model.Task;
import com.iescamp.studyflow_api.repository.SubjectRepository;
import com.iescamp.studyflow_api.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired private TaskRepository taskRepository;
    @Autowired private SubjectRepository subjectRepository;

    public TaskResponseDTO add(TaskResponseDTO dto) {
        Task task = new Task();
        return saveOrUpdate(task, dto);
    }

    public TaskResponseDTO modify(Integer id, TaskResponseDTO dto) {
        // Buscamos la tarea existente
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setStart_date(dto.getStart_date());
        task.setDue_date(dto.getDue_date());
        task.setCompleted(dto.isCompleted());

        // Actualizamos la relacion con la asignatura si ha cambiado
        if (dto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(dto.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Subject not found"));
            task.setSubjectId(subject);
        }

        return TaskResponseDTO.convertToDTO(taskRepository.save(task));
    }

    // Metodo privado para no repetir codigo entre add y modify
    private TaskResponseDTO saveOrUpdate(Task task, TaskResponseDTO dto) {
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setStart_date(dto.getStart_date());
        task.setDue_date(dto.getDue_date());
        task.setCompleted(dto.isCompleted());

        if (dto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(dto.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Subject not found"));
            task.setSubjectId(subject);
        }

        return TaskResponseDTO.convertToDTO(taskRepository.save(task));
    }

    public List<TaskResponseDTO> findAll(Integer userId) {
        List<Task> tasks;
        if (userId != null) {
            tasks = taskRepository.findBySubjectId_UserId(userId);
        } else {
            tasks = taskRepository.findAll();
        }
        return tasks.stream()
                .map(TaskResponseDTO::convertToDTO)
                .collect(Collectors.toList());
    }

    public void delete(Integer id) {
        if (!taskRepository.existsById(id)) throw new RuntimeException("Task not found");
        taskRepository.deleteById(id);
    }

    public TaskResponseDTO findById(Integer id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return TaskResponseDTO.convertToDTO(task);
    }
}
