package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.OperationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OperationHistoryRepository extends JpaRepository<OperationHistory, Integer> {
    List<OperationHistory> findByUserId(Integer userId);
}