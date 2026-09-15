package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.model.VoiceJournal;
import com.iescamp.studyflow_api.security.CurrentUserProvider;
import com.iescamp.studyflow_api.service.JournalService;
import com.iescamp.studyflow_api.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/journal")
public class JournalController {

    private static final long MAX_AUDIO_BYTES = 10 * 1024 * 1024; // 10 MB per voice note

    @Autowired
    private JournalService journalService;

    @Autowired
    private CurrentUserProvider currentUser;

    @Autowired
    private StorageService storageService;

    @PostMapping(value = "/ingest", consumes = "multipart/form-data")
    public ResponseEntity<?> ingest(
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "audio", required = false) MultipartFile audio,
            @RequestParam(value = "taskId", required = false) Integer taskId,
            @RequestParam(value = "durationSeconds", required = false) Integer durationSeconds) {

        try {
            // Store the raw audio now; STT transcription plugs in later.
            String audioPath = null;
            if (audio != null && !audio.isEmpty()) {
                if (audio.getSize() > MAX_AUDIO_BYTES) {
                    throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Audio too large");
                }
                audioPath = storageService.store(currentUser.getUserId(),
                        audio.getOriginalFilename(), audio.getBytes());
            }

            VoiceJournal entry = journalService.ingest(currentUser.getUserId(), text, taskId, audioPath, durationSeconds);
            return ResponseEntity.ok(entry);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Journal ingest failed: " + e.getMessage());
        }
    }

    /** Streams back the stored voice note - only to its owner. */
    @GetMapping("/{id}/audio")
    public ResponseEntity<?> audio(@PathVariable Integer id) {
        VoiceJournal entry = journalService.findOwned(currentUser.getUserId(), id);
        if (entry.getAudioPath() == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] data = storageService.retrieve(entry.getAudioPath());
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"voice-note\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(journalService.listForUser(currentUser.getUserId()));
    }

    @PostMapping("/{id}/actions")
    public ResponseEntity<?> promoteAction(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(
                    journalService.promoteAction(currentUser.getUserId(), id, body.get("text")));
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Could not create task: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            journalService.delete(currentUser.getUserId(), id);
            return ResponseEntity.ok("Deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
