package com.iescamp.studyflow.service;

import com.iescamp.studyflow.database.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

/**
 * Local-first focus session store. Written offline; pushed by SyncService.
 */
public class FocusService {

    public void recordSession(Integer taskId, int durationSeconds, boolean focus) throws Exception {
        LocalDateTime endedAt = LocalDateTime.now();
        LocalDateTime startedAt = endedAt.minusSeconds(durationSeconds);
        String mode = focus ? "FOCUS" : "BREAK";

        try (Connection conn = DatabaseHelper.getConnection()) {
            int id = DatabaseHelper.nextLocalId(conn, "FocusSessions", "sessionId");
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO FocusSessions (sessionId, taskId, startedAt, endedAt, durationSeconds, " +
                            "mode, completed, updatedAt, deletedAt, syncStatus, clientUuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, 1, ?, NULL, 'PENDING_SYNC', ?)")) {
                ps.setInt(1, id);
                if (taskId == null) ps.setObject(2, null);
                else ps.setInt(2, taskId);
                ps.setString(3, startedAt.toString());
                ps.setString(4, endedAt.toString());
                ps.setInt(5, durationSeconds);
                ps.setString(6, mode);
                ps.setString(7, endedAt.toString());
                ps.setString(8, java.util.UUID.randomUUID().toString());
                ps.executeUpdate();
            }
        }
    }
}
