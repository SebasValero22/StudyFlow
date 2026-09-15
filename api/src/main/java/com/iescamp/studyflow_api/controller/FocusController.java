package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.FocusReportDTO;
import com.iescamp.studyflow_api.model.FocusSession;
import com.iescamp.studyflow_api.security.CurrentUserProvider;
import com.iescamp.studyflow_api.service.FocusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/focus")
public class FocusController {

    @Autowired
    private FocusService focusService;

    @Autowired
    private CurrentUserProvider currentUser;

    @PostMapping("/sessions")
    public ResponseEntity<?> recordSession(@RequestBody Map<String, Object> body) {
        try {
            Integer taskId = body.get("taskId") == null ? null : ((Number) body.get("taskId")).intValue();
            Integer duration = body.get("durationSeconds") == null ? null : ((Number) body.get("durationSeconds")).intValue();
            String startedAtStr = (String) body.get("startedAt");
            LocalDateTime startedAt = startedAtStr != null ? LocalDateTime.parse(startedAtStr) : null;
            Boolean completed = body.get("completed") == null ? null : (Boolean) body.get("completed");

            FocusSession saved = focusService.record(currentUser.getUserId(), taskId,
                    startedAt, duration, (String) body.get("mode"), completed);
            return ResponseEntity.ok(saved);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid session payload: " + e.getMessage());
        }
    }

    @GetMapping("/sessions")
    public List<FocusSession> history(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from) {
        return focusService.history(currentUser.getUserId(), from);
    }

    @GetMapping("/report")
    public FocusReportDTO report(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return focusService.report(currentUser.getUserId(), from, to);
    }

    /** The timesheet: text/csv attachment, importable into Excel/Sheets/invoicing tools. */
    @GetMapping("/timesheet.csv")
    public ResponseEntity<String> timesheet(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (to.isBefore(from)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "'to' is before 'from'");
        }
        String csv = focusService.timesheetCsv(currentUser.getUserId(), from, to);
        String filename = "studyflow-timesheet-" + from + "_" + to + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.valueOf("text/csv"))
                .body(csv);
    }
}
