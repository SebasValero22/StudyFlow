package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.NyraIngestRequestDTO;
import com.iescamp.studyflow_api.dto.TaskResponseDTO;
import com.iescamp.studyflow_api.model.Task;
import com.iescamp.studyflow_api.security.CurrentUserProvider;
import com.iescamp.studyflow_api.service.NyraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nyra")
public class NyraController {

    @Autowired
    private NyraService nyraService;

    @Autowired
    private CurrentUserProvider currentUser;

    @PostMapping(value = "/ingest", consumes = {"multipart/form-data"})
    public ResponseEntity<?> ingestVoiceOrTextCommand(
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "audio", required = false) org.springframework.web.multipart.MultipartFile audio) {

        String finalText = text;

        if (audio != null && !audio.isEmpty()) {
            // TODO: In the future, pass 'audio' to an STT service (e.g., faster-whisper)
            System.out.println("Received audio blob of size: " + audio.getSize() + " bytes");
            if (finalText == null || finalText.isBlank()) {
                finalText = "Mock Transcription: Remind me to study German A2 this Friday"; // Temporary mock
            }
        }

        if (finalText == null || finalText.isBlank()) {
            return ResponseEntity.badRequest().body("Text or Audio input is required.");
        }

        try {
            NyraIngestRequestDTO request = new NyraIngestRequestDTO();
            request.setText(finalText);
            
            Task task = nyraService.processIngestRequest(request, currentUser.getUserId());
            if (task != null) {
                return ResponseEntity.ok(task);
            } else {
                return ResponseEntity.ok("Nyra processed the command but no task was created.");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing command: " + e.getMessage());
        }
    }

    @PostMapping("/auto-schedule")
    public ResponseEntity<?> autoScheduleWeek() {
        try {
            int updatedCount = nyraService.autoScheduleInbox(currentUser.getUserId());
            return ResponseEntity.ok(java.util.Map.of(
                "message", "Auto-scheduling complete.",
                "tasksUpdated", updatedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of(
                "error", "Failed to auto-schedule: " + e.getMessage()
            ));
        }
    }
}
