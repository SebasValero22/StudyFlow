package com.iescamp.studyflow.service;

import com.iescamp.studyflow.database.DatabaseHelper;
import com.iescamp.studyflow.utils.AppSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Queues syncable settings (theme/timer) into the local UserSettings outbox.
 * Device-local keys (cloud.apiUrl, notes.vaultDir) are never queued.
 */
public final class SettingsOutbox {

    private SettingsOutbox() {}

    public static void queueCurrentSettings() {
        AppSettings s = AppSettings.get();
        Map<String, String> values = new LinkedHashMap<>();
        values.put("ui.theme", s.getTheme());
        values.put("timer.preset", s.getTimerPreset().name());
        values.put("timer.focus", String.valueOf(s.getFocusMinutes()));
        values.put("timer.short", String.valueOf(s.getShortBreakMinutes()));
        values.put("timer.long", String.valueOf(s.getLongBreakMinutes()));
        values.put("timer.cycles", String.valueOf(s.getCyclesBeforeLongBreak()));
        values.put("timer.autostart.breaks", String.valueOf(s.isAutoStartBreaks()));
        values.put("timer.autostart.focus", String.valueOf(s.isAutoStartFocus()));
        values.put("timer.sound", String.valueOf(s.isSoundEnabled()));

        String now = LocalDateTime.now().toString();
        try (Connection conn = DatabaseHelper.getConnection()) {
            for (Map.Entry<String, String> e : values.entrySet()) {
                try (PreparedStatement exists = conn.prepareStatement(
                        "SELECT 1 FROM UserSettings WHERE settingKey = ?")) {
                    exists.setString(1, e.getKey());
                    boolean present;
                    try (var rs = exists.executeQuery()) {
                        present = rs.next();
                    }
                    if (present) {
                        try (PreparedStatement up = conn.prepareStatement(
                                "UPDATE UserSettings SET settingValue = ?, updatedAt = ?, syncStatus = 'PENDING_SYNC' " +
                                        "WHERE settingKey = ?")) {
                            up.setString(1, e.getValue());
                            up.setString(2, now);
                            up.setString(3, e.getKey());
                            up.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ins = conn.prepareStatement(
                                "INSERT INTO UserSettings (settingKey, settingValue, updatedAt, syncStatus) " +
                                        "VALUES (?, ?, ?, 'PENDING_SYNC')")) {
                            ins.setString(1, e.getKey());
                            ins.setString(2, e.getValue());
                            ins.setString(3, now);
                            ins.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Could not queue settings for sync: " + ex.getMessage());
        }
    }
}
