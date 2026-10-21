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

class SyncServiceTest {

    private SyncService syncService;

    @BeforeAll
    static void initVault() throws Exception {
        Path temp = Files.createTempDirectory("studyflow-sync-test");
        System.setProperty("studyflow.vault", temp.toString());
        DatabaseHelper.initializeSchema();
    }

    @BeforeEach
    void setUp() throws Exception {
        syncService = SyncService.getInstance();
        try (Connection conn = DatabaseHelper.getConnection()) {
            for (String t : new String[]{"LifeAreas", "Tasks", "FocusSessions", "Exams", "Grades", "Reminders"}) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + t)) {
                    ps.executeUpdate();
                }
            }
        }
    }

    @Test
    void applyIdMapRenamesNegativeTaskAndRemapsLifeAreaFk() throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            insert(conn, "INSERT INTO LifeAreas (lifeAreaId, name, active, updatedAt, syncStatus) " +
                    "VALUES (-1, 'Math', 1, ?, 'PENDING_SYNC')");
            insert(conn, "INSERT INTO Tasks (taskId, title, lifeAreaId, isCompleted, isHabit, orderIndex, updatedAt, syncStatus) " +
                    "VALUES (-5, 'Homework', -1, 0, 0, 0, ?, 'PENDING_SYNC')");
        }

        Map<String, Integer> idMap = new HashMap<>();
        idMap.put("lifeArea:-1", 100);
        idMap.put("task:-5", 200);
        syncService.applyIdMap(idMap);

        try (Connection conn = DatabaseHelper.getConnection()) {
            assertEquals(100, singleInt(conn,
                    "SELECT lifeAreaId FROM LifeAreas WHERE name = 'Math'"));
            assertEquals(200, singleInt(conn,
                    "SELECT taskId FROM Tasks WHERE title = 'Homework'"));
            assertEquals(100, singleInt(conn,
                    "SELECT lifeAreaId FROM Tasks WHERE taskId = 200"));
        }
    }

    @Test
    void applyIdMapRemapsExamAndGradeLifeAreaFks() throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            insert(conn, "INSERT INTO LifeAreas (lifeAreaId, name, active, updatedAt, syncStatus) " +
                    "VALUES (-2, 'History', 1, ?, 'PENDING_SYNC')");
            insert(conn, "INSERT INTO Exams (examId, subjectId, nameExam, updatedAt, syncStatus) " +
                    "VALUES (-10, -2, 'Final', ?, 'PENDING_SYNC')");
            insert(conn, "INSERT INTO Grades (gradeId, subjectId, concept, score, weight, updatedAt, syncStatus) " +
                    "VALUES (-20, -2, 'Essay', 9.0, 40.0, ?, 'PENDING_SYNC')");
        }

        Map<String, Integer> idMap = new HashMap<>();
        idMap.put("lifeArea:-2", 42);
        syncService.applyIdMap(idMap);

        try (Connection conn = DatabaseHelper.getConnection()) {
            assertEquals(42, singleInt(conn, "SELECT subjectId FROM Exams WHERE examId = -10"));
            assertEquals(42, singleInt(conn, "SELECT subjectId FROM Grades WHERE gradeId = -20"));
        }
    }

    @Test
    void lwwLocalPendingNewerThanRemoteWins() {
        String remote = LocalDateTime.now().minusMinutes(5).toString();
        String localNewer = LocalDateTime.now().toString();
        String localOlder = LocalDateTime.now().minusHours(1).toString();

        assertTrue(SyncService.isLocalNewer(localNewer, "PENDING_SYNC", remote));
        assertFalse(SyncService.isLocalNewer(localOlder, "PENDING_SYNC", remote));
        assertFalse(SyncService.isLocalNewer(localNewer, "SYNCED", remote));
        assertFalse(SyncService.isLocalNewer(null, "PENDING_SYNC", remote));
        assertFalse(SyncService.isLocalNewer(localNewer, "PENDING_SYNC", null));
    }

    @Test
    void protocolVersionIsTwo() {
        assertEquals(2, SyncService.PROTOCOL_VERSION);
    }

    @Test
    void rejectedKeysParsesPushResponseBody() {
        Map<String, Object> body = new HashMap<>();
        assertTrue(SyncService.rejectedKeys(body).isEmpty());
        assertTrue(SyncService.rejectedKeys(null).isEmpty());

        Map<String, String> rejected = new HashMap<>();
        rejected.put("task:-5", "title required");
        body.put("rejected", rejected);
        assertEquals(java.util.Set.of("task:-5"), SyncService.rejectedKeys(body));
    }

    @Test
    void markPushedRowsSyncedSkipsRejectedRows() throws Exception {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO Tasks (taskId, title, isCompleted, isHabit, orderIndex, updatedAt, syncStatus) " +
                             "VALUES (-1, 'Accepted', 0, 0, 0, ?, 'PENDING_SYNC')")) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO Tasks (taskId, title, isCompleted, isHabit, orderIndex, updatedAt, syncStatus) " +
                             "VALUES (-2, 'Rejected', 0, 0, 0, ?, 'PENDING_SYNC')")) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.executeUpdate();
        }

        java.util.List<Map<String, Object>> pushed = java.util.List.of(
                Map.of("localId", -1), Map.of("localId", -2));
        syncService.markPushedRowsSynced(
                List.of(), pushed, List.of(), List.of(), List.of(), List.of(), List.of(),
                java.util.Set.of("task:-2"));

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT taskId, syncStatus FROM Tasks WHERE taskId IN (-1, -2)")) {
            ResultSet rs = ps.executeQuery();
            Map<Integer, String> status = new HashMap<>();
            while (rs.next()) {
                status.put(rs.getInt("taskId"), rs.getString("syncStatus"));
            }
            assertEquals("SYNCED", status.get(-1), "accepted row must be marked synced");
            assertEquals("PENDING_SYNC", status.get(-2), "rejected row must stay pending");
        }
    }

    // ---- helpers ------------------------------------------------------

    private static void insert(Connection conn, String sqlWithPlaceholders) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sqlWithPlaceholders)) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    private static int singleInt(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            return rs.getInt(1);
        }
    }
}
