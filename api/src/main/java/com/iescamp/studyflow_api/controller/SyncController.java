package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.SyncPayloadDTO;
import com.iescamp.studyflow_api.dto.SyncPushResponseDTO;
import com.iescamp.studyflow_api.security.CurrentUserProvider;
import com.iescamp.studyflow_api.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    /** Bump when push/pull payload shape changes incompatibly. */
    public static final int PROTOCOL_VERSION = 2;

    private final SyncService syncService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/pull")
    public ResponseEntity<SyncPayloadDTO> pullSync(@RequestParam(required = false) String since) {
        Integer userId = currentUserProvider.getUserId();
        LocalDateTime sinceDt = parseSince(since);
        SyncPayloadDTO payload = syncService.pullSince(userId, sinceDt);
        payload.setProtocolVersion(PROTOCOL_VERSION);
        return ResponseEntity.ok(payload);
    }

    @PostMapping("/push")
    public ResponseEntity<SyncPushResponseDTO> pushSync(@RequestBody SyncPayloadDTO payload) {
        Integer userId = currentUserProvider.getUserId();
        if (payload.getProtocolVersion() != null
                && payload.getProtocolVersion() > PROTOCOL_VERSION) {
            return ResponseEntity.badRequest().body(new SyncPushResponseDTO());
        }
        SyncPushResponseDTO response = syncService.pushBatch(userId, payload);
        response.setProtocolVersion(PROTOCOL_VERSION);
        return ResponseEntity.ok(response);
    }

    /**
     * Accepts bare ISO LocalDateTime (Jackson default) or offset form;
     * null/blank = full sync (handled by SyncService).
     */
    private static LocalDateTime parseSince(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String value = raw.trim();
        // Strip a trailing offset (Z or +01:00) if a client sent one.
        int plus = value.indexOf('+', 10);
        if (value.endsWith("Z")) {
            value = value.substring(0, value.length() - 1);
        } else if (plus > 0) {
            value = value.substring(0, plus);
        }
        // Truncate fractional seconds to what LocalDateTime.parse accepts with ISO_LOCAL_DATE_TIME
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            // Fallback: try with nanos trimmed
            int dot = value.indexOf('.');
            if (dot > 0 && value.length() > dot + 8) {
                value = value.substring(0, dot + 7);
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            throw e;
        }
    }
}

