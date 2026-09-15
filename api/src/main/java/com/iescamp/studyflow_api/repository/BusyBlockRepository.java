package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.BusyBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BusyBlockRepository extends JpaRepository<BusyBlock, Integer> {
    void deleteByFeedId(Integer feedId);
    List<BusyBlock> findByFeedIdInAndStartAtLessThanAndEndAtGreaterThanEqual(
            List<Integer> feedIds, LocalDateTime startBefore, LocalDateTime endAfter);
}
