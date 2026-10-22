package com.iescamp.studyflow.service;

import com.iescamp.studyflow.database.DatabaseHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SyncConflictServiceTest {

    private SyncService syncService;

    @BeforeAll
    static void initVault() throws Exception {
        Path temp = Files.createTempDirectory("studyflow-conflict-test");
        System.setProperty("studyflow.vault", temp.toString());
        DatabaseHelper.initializeSchema();
    }

    @BeforeEach
    void setUp() throws Exception {
        syncService = SyncService.getInstance();
        try (Connection conn = DatabaseHelper.getConnection()) {
            for (String t : new String[]{"Tasks", "SyncConflicts"}) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + t)) {
                    ps.executeUpdate();
                }
            }
        }
    }

    @Test
    void remoteNewerOverwritesPendingLocalAndRecordsConflict() throws Exception {
        String localAt = LocalDateTime.now().minusHours(2).toString();
        String remoteAt = LocalDateTime.now().toString();
        insertTask(7, "My unsynced edit", localAt, "PENDING_SYNC");

        Map<String, Object> remote = new HashMap<>();
        remote.put("taskId", 7);
        remote.put("title", "Cloud version");
        remote.put("updatedAt", remoteAt);
        remote.put("isCompleted", false);
        remote.put("isHabit", false);
        remote.put("orderIndex", 0);
        syncService.mergeTasks(List.of(remote));

        assertEquals("Cloud version", taskTitle(7));
        assertEquals("SYNCED", taskStatus(7));

        List<SyncConflictService.Conflict> conflicts = SyncConflictService.list();
        assertEquals(1, conflicts.size());
        assertEquals("Task", conflicts.get(0).entityType());
        assertEquals(7, conflicts.get(0).entityId());
        assertEquals("My unsynced edit", conflicts.get(0).localSummary());
        assertEquals("Cloud version", conflicts.get(0).remoteSummary());
    }

    @Test
    void newerPendingLocalWinsAndNoConflictIsRecorded() throws Exception {
        String localAt = LocalDateTime.now().toString();
        String remoteAt = LocalDateTime.now().minusHours(2).toString();
        insertTask(9, "Fresher local edit", localAt, "PENDING_SYNC");

        Map<String, Object> remote = new HashMap<>();
        remote.put("taskId", 9);
        remote.put("title", "Stale cloud version");
        remote.put("updatedAt", remoteAt);
        remote.put("isCompleted", false);
        remote.put("isHabit", false);
        remote.put("orderIndex", 0);
        syncService.mergeTasks(List.of(remote));

        assertEquals("Fresher local edit", taskTitle(9));
        assertEquals("PENDING_SYNC", taskStatus(9));
        assertEquals(0, SyncConflictService.activeCount());
    }

    @Test
    void syncedLocalLosesToRemoteWithoutConflict() throws Exception {
        insertTask(3, "Already synced", LocalDateTime.now().minusDays(1).toString(), "SYNCED");

        Map<String, Object> remote = new HashMap<>();
        remote.put("taskId", 3);
        remote.put("title", "Updated elsewhere");
        remote.put("updatedAt", LocalDateTime.now().toString());
        remote.put("isCompleted", false);
        remote.put("isHabit", false);
        remote.put("orderIndex", 0);
        syncService.mergeTasks(List.of(remote));

        assertEquals("Updated elsewhere", taskTitle(3));
        assertEquals(0, SyncConflictService.activeCount(), "normal update is not a conflict");
    }

    @Test
    void repeatConflictForSameRowReplacesPreviousEntry() throws Exception {
        insertTask(5, "v1", LocalDateTime.now().minusDays(2).toString(), "PENDING_SYNC");
        Map<String, Object> remote = new HashMap<>();
        remote.put("taskId", 5);
        remote.put("title", "cloud-a");
        remote.put("updatedAt", LocalDateTime.now().minusMinutes(30).toString());
        remote.put("isCompleted", false);
        remote.put("isHabit", false);
        remote.put("orderIndex", 0);
        syncService.mergeTasks(List.of(remote));

        insertTask(5, "v2", LocalDateTime.now().toString(), "PENDING_SYNC");
        remote.put("title", "cloud-b");
        remote.put("updatedAt", LocalDateTime.now().toString());
        syncService.mergeTasks(List.of(remote));

        assertEquals(1, SyncConflictService.activeCount());
        assertEquals("cloud-b", SyncConflictService.list().get(0).remoteSummary());
    }

    @Test
    void dismissAllClearsActiveCount() throws Exception {
        insertTask(11, "gone", LocalDateTime.now().minusHours(1).toString(), "PENDING_SYNC");
        Map<String, Object> remote = new HashMap<>();
        remote.put("taskId", 11);
        remote.put("title", "winner");
        remote.put("updatedAt", LocalDateTime.now().toString());
        remote.put("isCompleted", false);
        remote.put("isHabit", false);
        remote.put("orderIndex", 0);
        syncService.mergeTasks(List.of(remote));

        assertEquals(1, SyncConflictService.activeCount());
        SyncConflictService.dismissAll();
        assertEquals(0, SyncConflictService.activeCount());
        assertTrue(SyncConflictService.list().isEmpty());
    }

    // ---- helpers ------------------------------------------------------

    private static void insertTask(int id, String title, String updatedAt, String status) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement del = conn.prepareStatement("DELETE FROM Tasks WHERE taskId = ?")) {
            del.setInt(1, id);
            del.executeUpdate();
        }
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO Tasks (taskId, title, isCompleted, isHabit, orderIndex, updatedAt, syncStatus) " +
                             "VALUES (?, ?, 0, 0, 0, ?, ?)")) {
            ps.setInt(1, id);
            ps.setString(2, title);
            ps.setString(3, updatedAt);
            ps.setString(4, status);
            ps.executeUpdate();
        }
    }

    private static String taskTitle(int id) throws Exception {
        return taskColumn(id, "title");
    }

    private static String taskStatus(int id) throws Exception {
        return taskColumn(id, "syncStatus");
    }

    private static String taskColumn(int id, String column) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT " + column + " FROM Tasks WHERE taskId = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getString(1);
            }
        }
    }
}
