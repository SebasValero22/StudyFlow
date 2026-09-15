package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.dto.TaskResponseDTO;
import com.iescamp.studyflow_api.model.VoiceJournal;
import com.iescamp.studyflow_api.model.enums.Mood;
import com.iescamp.studyflow_api.repository.VoiceJournalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class JournalService {

    private static final String CLEANUP_PROMPT = """
            VOICE JOURNAL CLEANUP. You are Nyra, the journaling assistant for StudyFlow.
            Clean up the following raw voice transcription: strip filler words and repetitions,
            format the result as clean Markdown (short paragraphs), and append an "Actions" section
            with "- [ ]" checkboxes for any actionable items you detect.
            Also infer the writer's mood, choosing exactly one of:
            GREAT, GOOD, NEUTRAL, ROUGH, TERRIBLE.
            Return ONLY a strict JSON object with this schema, no markdown fences:
            {"title": "short title, max 60 chars", "transcript": "cleaned markdown", "moodTag": "GREAT|GOOD|NEUTRAL|ROUGH|TERRIBLE"}

            RAW TRANSCRIPTION:
            """;

    private final VoiceJournalRepository repository;
    private final GeminiClient geminiClient;
    private final TaskService taskService;

    public JournalService(VoiceJournalRepository repository,
                          GeminiClient geminiClient,
                          @Lazy TaskService taskService) {
        this.repository = repository;
        this.geminiClient = geminiClient;
        this.taskService = taskService;
    }

    public VoiceJournal ingest(Integer userId, String rawText, Integer taskId,
                               String audioPath, Integer durationSeconds) {
        // 1. Transcribe audio -> STT pipeline (placeholder until faster-whisper is wired)
        String rawTranscript = rawText;
        if (rawTranscript == null || rawTranscript.isBlank()) {
            rawTranscript = "Mock transcription: reflected on today's study session.";
        }

        // 2. Ask Gemini to clean up + structure + infer mood
        Map<String, Object> cleaned = cleanup(rawTranscript);

        // 3. Persist
        VoiceJournal entry = new VoiceJournal();
        entry.setUserId(userId);
        entry.setTaskId(taskId);
        entry.setAudioPath(audioPath);
        entry.setDurationSeconds(durationSeconds);
        entry.setRawTranscript(rawTranscript);
        entry.setTitle((String) cleaned.getOrDefault("title", "Voice Journal"));
        entry.setTranscript((String) cleaned.getOrDefault("transcript", rawTranscript));
        entry.setMoodTag(parseMood((String) cleaned.get("moodTag")));

        return repository.save(entry);
    }

    /**
     * Bridges reflection to execution: promotes a journal action item into a real task.
     * The entry must belong to the caller; the created task lands in their inbox.
     */
    public TaskResponseDTO promoteAction(Integer userId, Integer entryId, String actionText) {
        VoiceJournal entry = findOwned(userId, entryId);

        String title = actionText == null ? "" : actionText.trim();
        if (title.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action text is required");
        }
        if (title.length() > 100) {
            title = title.substring(0, 97) + "...";
        }

        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setTitle(title);
        dto.setDescription("From journal entry #" + entryId);
        dto.setCompleted(false);
        dto.setPriority(com.iescamp.studyflow_api.model.enums.Priority.MEDIUM);
        // No due_date / lifeAreaId: lands in the user's inbox for Nyra to schedule later.
        return taskService.add(userId, dto);
    }

    public List<VoiceJournal> listForUser(Integer userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void delete(Integer userId, Integer entryId) {
        repository.delete(findOwned(userId, entryId));
    }

    /** Cross-account access is indistinguishable from "does not exist". */
    public VoiceJournal findOwned(Integer userId, Integer entryId) {
        return repository.findById(entryId)
                .filter(e -> e.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> cleanup(String rawTranscript) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(geminiClient.completeJson(CLEANUP_PROMPT + rawTranscript), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    private Mood parseMood(String mood) {
        if (mood == null) return null;
        try {
            return Mood.valueOf(mood.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // DB CHECK constraint also guards this
        }
    }
}
