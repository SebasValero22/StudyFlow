package com.iescamp.studyflow_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.iescamp.studyflow_api.model.LifeArea;
import java.util.List;

public interface LifeAreaRepository extends JpaRepository<LifeArea, Integer> {
    List<LifeArea> findByUserId(Integer userId);
    List<LifeArea> findByName(String name);
}
