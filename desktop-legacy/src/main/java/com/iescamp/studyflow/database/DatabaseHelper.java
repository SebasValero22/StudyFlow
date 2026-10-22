package com.iescamp.studyflow.database;

import com.iescamp.studyflow.utils.ConfigLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Local-first SQLite store. Machine is the source of truth; cloud is a replica.
 * Negative primary keys = created offline, waiting for server id remap.
 */
public class DatabaseHelper {

    private static final String DB_NAME = "studyflow.db";

    private static String getUrl() throws IOException {
        Path vault = ConfigLoader.getVaultDir();
        Files.createDirectories(vault);
        Path dbPath = vault.resolve(DB_NAME);
        return "jdbc:sqlite:" + dbPath.toAbsolutePath();
    }

    public static Connection getConnection() throws SQLException, IOException {
        return DriverManager.getConnection(getUrl());
    }

    public static void initializeSchema() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS LifeAreas (" +
                    "lifeAreaId INTEGER PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "color TEXT," +
                    "active INTEGER DEFAULT 1," +
                    "academicYear TEXT," +
                    "updatedAt TEXT," +
                    "deletedAt TEXT," +
                    "syncStatus TEXT DEFAULT 'SYNCED'," +
                    "clientUuid TEXT" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Tasks (" +
                    "taskId INTEGER PRIMARY KEY," +
                    "title TEXT NOT NULL," +
                    "description TEXT," +
                    "isCompleted INTEGER DEFAULT 0," +
                    "isHabit INTEGER DEFAULT 0," +
                    "priority TEXT," +
                    "startDate TEXT," +
                    "dueDate TEXT," +
                    "lifeAreaId INTEGER," +
                    "projectId INTEGER," +
                    "orderIndex INTEGER DEFAULT 0," +
                    "updatedAt TEXT," +
                    "deletedAt TEXT," +
                    "syncStatus TEXT DEFAULT 'SYNCED'," +
                    "clientUuid TEXT," +
                    "trigger TEXT," +
                    "microStep TEXT," +
                    "energyBucket TEXT" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS FocusSessions (" +
                    "sessionId INTEGER PRIMARY KEY," +
                    "taskId INTEGER," +
                    "startedAt TEXT," +
                    "endedAt TEXT," +
                    "durationSeconds INTEGER," +
                    "mode TEXT," +
                    "completed INTEGER DEFAULT 1," +
                    "updatedAt TEXT," +
                    "deletedAt TEXT," +
                    "syncStatus TEXT DEFAULT 'SYNCED'," +
                    "clientUuid TEXT" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Exams (" +
                    "examId INTEGER PRIMARY KEY," +
                    "subjectId INTEGER," +
                    "nameExam TEXT NOT NULL," +
                    "examType TEXT," +
                    "examDate TEXT," +
                    "classroom TEXT," +
                    "updatedAt TEXT," +
                    "deletedAt TEXT," +
                    "syncStatus TEXT DEFAULT 'SYNCED'," +
                    "clientUuid TEXT" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Grades (" +
                    "gradeId INTEGER PRIMARY KEY," +
                    "subjectId INTEGER," +
                    "concept TEXT," +
                    "score REAL," +
                    "weight REAL," +
                    "gradeDate TEXT," +
                    "updatedAt TEXT," +
                    "deletedAt TEXT," +
                    "syncStatus TEXT DEFAULT 'SYNCED'," +
                    "clientUuid TEXT" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS Reminders (" +
                    "reminderId INTEGER PRIMARY KEY," +
                    "message TEXT," +
                    "dateTime TEXT," +
                    "taskId INTEGER," +
                    "examId INTEGER," +
                    "updatedAt TEXT," +
                    "deletedAt TEXT," +
                    "syncStatus TEXT DEFAULT 'SYNCED'," +
                    "clientUuid TEXT" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS UserSettings (" +
                    "settingKey TEXT PRIMARY KEY," +
                    "settingValue TEXT," +
                    "updatedAt TEXT," +
                    "syncStatus TEXT DEFAULT 'SYNCED'" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS SyncConflicts (" +
                    "conflictId INTEGER PRIMARY KEY," +
                    "entityType TEXT NOT NULL," +
                    "entityId INTEGER NOT NULL," +
                    "localSummary TEXT," +
                    "remoteSummary TEXT," +
                    "localUpdatedAt TEXT," +
                    "remoteUpdatedAt TEXT," +
                    "detectedAt TEXT," +
                    "dismissed INTEGER DEFAULT 0" +
                    ")");

            ensureColumns(conn, "Tasks",
                    "startDate TEXT", "dueDate TEXT", "lifeAreaId INTEGER",
                    "projectId INTEGER", "orderIndex INTEGER DEFAULT 0",
                    "updatedAt TEXT", "deletedAt TEXT", "syncStatus TEXT DEFAULT 'SYNCED'",
                    "isHabit INTEGER DEFAULT 0", "description TEXT", "clientUuid TEXT",
                    "trigger TEXT", "microStep TEXT", "energyBucket TEXT");
            ensureColumns(conn, "FocusSessions",
                    "endedAt TEXT", "updatedAt TEXT", "deletedAt TEXT",
                    "syncStatus TEXT DEFAULT 'SYNCED'", "clientUuid TEXT");
            ensureColumns(conn, "LifeAreas",
                    "updatedAt TEXT", "deletedAt TEXT", "syncStatus TEXT DEFAULT 'SYNCED'",
                    "color TEXT", "active INTEGER DEFAULT 1", "academicYear TEXT",
                    "clientUuid TEXT");
            ensureColumns(conn, "UserSettings",
                    "updatedAt TEXT", "syncStatus TEXT DEFAULT 'SYNCED'");
            ensureColumns(conn, "Exams",
                    "updatedAt TEXT", "deletedAt TEXT", "syncStatus TEXT DEFAULT 'SYNCED'",
                    "clientUuid TEXT", "subjectId INTEGER", "classroom TEXT");
            ensureColumns(conn, "Grades",
                    "updatedAt TEXT", "deletedAt TEXT", "syncStatus TEXT DEFAULT 'SYNCED'",
                    "clientUuid TEXT", "subjectId INTEGER", "gradeDate TEXT");
            ensureColumns(conn, "Reminders",
                    "updatedAt TEXT", "deletedAt TEXT", "syncStatus TEXT DEFAULT 'SYNCED'",
                    "clientUuid TEXT", "message TEXT", "dateTime TEXT",
                    "taskId INTEGER", "examId INTEGER");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.err.println("Could not initialize local SQLite database: " + e.getMessage());
        }
    }

    /** Wipe all synced + pending rows (used when the logged-in user changes). */
    public static void wipeLocalData() {
        String[] tables = {"LifeAreas", "Tasks", "FocusSessions", "Exams", "Grades", "Reminders",
                "UserSettings", "SyncConflicts", "VaultItems"};
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String t : tables) {
                stmt.executeUpdate("DELETE FROM " + t);
            }
        } catch (SQLException | IOException e) {
            System.err.println("Could not wipe local data: " + e.getMessage());
        }
    }

    private static void ensureColumns(Connection conn, String table, String... columnDefs)
            throws SQLException {
        List<String> existing = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                existing.add(rs.getString("name").toLowerCase());
            }
        }
        if (existing.isEmpty()) return;
        try (Statement stmt = conn.createStatement()) {
            for (String def : columnDefs) {
                String col = def.split("\\s+")[0];
                if (!existing.contains(col.toLowerCase())) {
                    stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + def);
                }
            }
        }
    }

    /** Next offline id: -1, -2, ... (never collides with server ids). */
    public static int nextLocalId(Connection conn, String table, String pkColumn) throws SQLException {
        try (var ps = conn.prepareStatement(
                "SELECT MIN(" + pkColumn + ") FROM " + table + " WHERE " + pkColumn + " < 0")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int min = rs.getInt(1);
                    if (!rs.wasNull() && min < 0) return min - 1;
                }
            }
        }
        return -1;
    }
}
