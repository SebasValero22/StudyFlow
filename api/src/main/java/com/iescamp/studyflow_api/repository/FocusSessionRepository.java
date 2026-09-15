package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FocusSessionRepository extends JpaRepository<FocusSession, Integer> {

    List<FocusSession> findByUserIdAndStartedAtGreaterThanEqualOrderByStartedAtDesc(
            Integer userId, LocalDateTime from);

    @Query("SELECT COALESCE(SUM(s.durationSeconds), 0) FROM FocusSession s " +
           "WHERE s.userId = :userId AND s.mode = 'FOCUS' " +
           "AND s.startedAt >= :from AND s.startedAt < :to")
    long sumFocusSeconds(@Param("userId") Integer userId,
                         @Param("from") LocalDateTime from,
                         @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(s) FROM FocusSession s " +
           "WHERE s.userId = :userId AND s.mode = 'FOCUS' " +
           "AND s.startedAt >= :from AND s.startedAt < :to")
    long countFocusSessions(@Param("userId") Integer userId,
                            @Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to);
}
