package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.dto.TaskReorderDTO;
import com.iescamp.studyflow_api.dto.TaskResponseDTO;
import com.iescamp.studyflow_api.model.LifeArea;
import com.iescamp.studyflow_api.model.Project;
import com.iescamp.studyflow_api.model.Task;
import com.iescamp.studyflow_api.repository.LifeAreaRepository;
import com.iescamp.studyflow_api.repository.ProjectRepository;
import com.iescamp.studyflow_api.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired private TaskRepository taskRepository;
    @Autowired private LifeAreaRepository lifeAreaRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectService projectService;

    public TaskResponseDTO add(Integer userId, TaskResponseDTO dto) {
        Task task = new Task();
        task.setUserId(userId);
        return saveOrUpdate(userId, task, dto);
    }

    public TaskResponseDTO modify(Integer userId, Integer id, TaskResponseDTO dto) {
        Task task = findVisibleForWrite(userId, id);
        return saveOrUpdate(userId, task, dto);
    }

    private TaskResponseDTO saveOrUpdate(Integer userId, Task task, TaskResponseDTO dto) {
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setStart_date(dto.getStart_date());
        task.setDue_date(dto.getDue_date());
        task.setCompleted(dto.isCompleted());

        if (dto.getOrderIndex() != null) {
            task.setOrderIndex(dto.getOrderIndex());
        }

        if (dto.getLifeAreaId() != null) {
            LifeArea lifeArea = lifeAreaRepository.findById(dto.getLifeAreaId())
                    .filter(area -> area.getUserId().equals(userId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "LifeArea not found"));
            task.setLifeArea(lifeArea);
        } else {
            task.setLifeArea(null);
        }

        if (dto.getProjectId() != null) {
            Project project = projectRepository.findById(dto.getProjectId())
                    .filter(p -> projectService.canWrite(userId, p.getProjectId()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project not found or not writable"));
            task.setProject(project);
        } else if (task.getProject() == null || !projectService.canWrite(userId, task.getProject().getProjectId())) {
            task.setProject(null);
        }

        return TaskResponseDTO.convertToDTO(taskRepository.save(task));
    }

    @Transactional
    public void reorderTasks(Integer userId, List<TaskReorderDTO> reorderRequests) {
        for (TaskReorderDTO req : reorderRequests) {
            taskRepository.findById(req.getTaskId())
                    .filter(t -> isWritable(userId, t))
                    .ifPresent(task -> {
                        task.setOrderIndex(req.getOrderIndex());
                        taskRepository.save(task);
                    });
        }
    }

    /** Own tasks + tasks in projects shared with the user. */
    public List<TaskResponseDTO> findAll(Integer userId) {
        List<Integer> sharedProjects = projectService.readableProjectIds(userId);
        List<Task> tasks = sharedProjects.isEmpty()
                ? taskRepository.findByUserIdOrderByOrderIndexAsc(userId)
                : taskRepository.findVisibleTasks(userId, sharedProjects);
        return tasks.stream().map(TaskResponseDTO::convertToDTO).collect(Collectors.toList());
    }

    public List<TaskResponseDTO> findInboxTasks(Integer userId) {
        return taskRepository.findInboxTasksByUser(userId).stream()
                .map(TaskResponseDTO::convertToDTO)
                .collect(Collectors.toList());
    }

    public void delete(Integer userId, Integer id) {
        Task task = findVisibleForWrite(userId, id);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        task.setDeletedAt(now);
        task.setUpdatedAt(now);
        taskRepository.save(task);
    }

    public TaskResponseDTO findById(Integer userId, Integer id) {
        return TaskResponseDTO.convertToDTO(findReadable(userId, id));
    }

    // ---- access model -------------------------------------------------

    private boolean isWritable(Integer userId, Task task) {
        if (task.getUserId().equals(userId)) return true;                 // personal tasks
        Integer projectId = task.getProject() != null ? task.getProject().getProjectId() : null;
        return projectId != null && projectService.canWrite(userId, projectId); // shared EDITOR/OWNER
    }

    private Task findReadable(Integer userId, Integer id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> notFound());
        boolean readable = isWritable(userId, task)
                || (task.getProject() != null && projectService.canRead(userId, task.getProject().getProjectId()));
        if (!readable) throw notFound();
        return task;
    }

    private Task findVisibleForWrite(Integer userId, Integer id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> notFound());
        if (!isWritable(userId, task)) throw notFound();
        return task;
    }

    /** Cross-account access is indistinguishable from "does not exist". */
    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
    }
}
