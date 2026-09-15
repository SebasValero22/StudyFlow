package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.Task;
import com.iescamp.studyflow_api.model.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByUserIdOrderByOrderIndexAsc(Integer userId);
    List<Task> findByPriority(Priority priority);

    @Query("SELECT t FROM Task t WHERE t.userId = :userId OR (t.project.projectId IN :sharedProjectIds) ORDER BY t.orderIndex ASC")
    List<Task> findVisibleTasks(@Param("userId") Integer userId, @Param("sharedProjectIds") List<Integer> sharedProjectIds);

    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.due_date IS NULL AND t.project IS NULL ORDER BY t.orderIndex ASC")
    List<Task> findInboxTasksByUser(Integer userId);

    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.due_date IS NULL ORDER BY t.orderIndex ASC")
    List<Task> findBacklogTasksByUser(Integer userId);
}
