package com.iescamp.studyflow_api.dto;
import com.iescamp.studyflow_api.model.Task;
import com.iescamp.studyflow_api.model.enums.Priority;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class TaskResponseDTO {
        private Integer taskId;
        private String title;
        private String description;
        private Priority priority;
        private LocalDate start_date;
        private LocalDate due_date;
        private boolean isCompleted;

        public static TaskResponseDTO convertToDTO(Task task) {
            TaskResponseDTO dto = new TaskResponseDTO();
            dto.setTaskId(task.getTaskId());
            dto.setTitle(task.getTitle());
            dto.setDescription(task.getDescription());
            dto.setPriority(task.getPriority());
            dto.setStart_date(task.getStart_date());
            dto.setDue_date(task.getDue_date());
            dto.setCompleted(task.getIsCompleted());
            return dto;
        }
}
