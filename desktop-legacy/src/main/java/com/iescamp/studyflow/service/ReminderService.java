package com.iescamp.studyflow.service;

import com.iescamp.studyflow.database.DatabaseHelper;
import com.iescamp.studyflow.model.Reminder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Local-first reminder store (SQLite). Mutations mark PENDING_SYNC; SyncService pushes them.
 */
public class ReminderService {

    public List<Reminder> getAllReminders() throws Exception {
        List<Reminder> reminders = new ArrayList<>();
        String sql = "SELECT reminderId, message, dateTime, taskId, examId, " +
                "updatedAt, deletedAt, syncStatus, clientUuid " +
                "FROM Reminders WHERE deletedAt IS NULL ORDER BY dateTime ASC, reminderId ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reminders.add(mapReminder(rs));
            }
        }
        return reminders;
    }

    public void saveReminder(Reminder reminder) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            Integer id = reminder.getReminderId();
            if (id == null || id <= 0) {
                id = DatabaseHelper.nextLocalId(conn, "Reminders", "reminderId");
                reminder.setReminderId(id);
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Reminders (reminderId, message, dateTime, taskId, examId, " +
                            "updatedAt, deletedAt, syncStatus, clientUuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, NULL, 'PENDING_SYNC', ?)")) {
                ps.setInt(1, id);
                ps.setString(2, reminder.getMessage());
                ps.setString(3, reminder.getDateTime() != null ? reminder.getDateTime().toString() : null);
                if (reminder.getTaskId() == null) ps.setObject(4, null);
                else ps.setInt(4, reminder.getTaskId());
                if (reminder.getExamId() == null) ps.setObject(5, null);
                else ps.setInt(5, reminder.getExamId());
                ps.setString(6, now.toString());
                ps.setString(7, reminder.getClientUuid() != null ? reminder.getClientUuid() : UUID.randomUUID().toString());
                ps.executeUpdate();
            }
        }
    }

    public void updateReminder(Integer id, Reminder reminder) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Reminders SET message = ?, dateTime = ?, taskId = ?, examId = ?, " +
                            "updatedAt = ?, syncStatus = 'PENDING_SYNC' WHERE reminderId = ?")) {
                ps.setString(1, reminder.getMessage());
                ps.setString(2, reminder.getDateTime() != null ? reminder.getDateTime().toString() : null);
                if (reminder.getTaskId() == null) ps.setObject(3, null);
                else ps.setInt(3, reminder.getTaskId());
                if (reminder.getExamId() == null) ps.setObject(4, null);
                else ps.setInt(4, reminder.getExamId());
                ps.setString(5, now.toString());
                ps.setInt(6, id);
                ps.executeUpdate();
            }
        }
    }

    public void deleteReminder(Integer id) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Reminders SET deletedAt = ?, updatedAt = ?, syncStatus = 'PENDING_SYNC' WHERE reminderId = ?")) {
                ps.setString(1, now.toString());
                ps.setString(2, now.toString());
                ps.setInt(3, id);
                ps.executeUpdate();
            }
        }
    }

    private static Reminder mapReminder(ResultSet rs) throws SQLException {
        Reminder r = new Reminder();
        r.setReminderId(rs.getInt("reminderId"));
        r.setMessage(rs.getString("message"));
        String dt = rs.getString("dateTime");
        if (dt != null) r.setDateTime(LocalDateTime.parse(dt));
        int taskId = rs.getInt("taskId");
        if (!rs.wasNull()) r.setTaskId(taskId);
        int examId = rs.getInt("examId");
        if (!rs.wasNull()) r.setExamId(examId);
        String updatedAt = rs.getString("updatedAt");
        if (updatedAt != null) r.setUpdatedAt(LocalDateTime.parse(updatedAt));
        String deletedAt = rs.getString("deletedAt");
        if (deletedAt != null) r.setDeletedAt(LocalDateTime.parse(deletedAt));
        r.setSyncStatus(rs.getString("syncStatus"));
        r.setClientUuid(rs.getString("clientUuid"));
        return r;
    }
}
