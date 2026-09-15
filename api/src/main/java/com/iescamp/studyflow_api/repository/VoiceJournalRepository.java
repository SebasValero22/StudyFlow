package com.iescamp.studyflow_api.repository;

import com.iescamp.studyflow_api.model.VoiceJournal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoiceJournalRepository extends JpaRepository<VoiceJournal, Integer> {
    List<VoiceJournal> findByUserIdOrderByCreatedAtDesc(Integer userId);
}
