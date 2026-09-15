package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.CalendarFeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarFeedRepository extends JpaRepository<CalendarFeed, Integer> {
    List<CalendarFeed> findByUserId(Integer userId);
}
