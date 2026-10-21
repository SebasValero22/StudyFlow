package com.iescamp.studyflow.service;

import com.iescamp.studyflow.database.DatabaseHelper;
import com.iescamp.studyflow.utils.AppSettings;
import com.iescamp.studyflow.utils.JsonMapper;
import com.iescamp.studyflow.utils.UserSession;
import com.iescamp.studyflow.model.User;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Local-first sync engine.
 * Push: PENDING_SYNC outbox → POST /api/sync/push → apply idMap (negative → server id).
 * Pull: GET /api/sync/pull?since=cursor → LWW merge → store serverTime cursor.
 * Guards: no-op without JWT; pauses after 401 / JWT expiry; exponential backoff on errors.
 */
public class SyncService {

    /** Wire protocol version. Bump when push/pull shape changes incompatibly. */
    public static final int PROTOCOL_VERSION = 2;

    private static final long INTERVAL_SECONDS = 60;
    private static final long MAX_BACKOFF_SECONDS = 15 * 60;

    private static final SyncService INSTANCE = new SyncService();

    public static SyncService getInstance() {
        return INSTANCE;
    }

    private SyncService() {}

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "studyflow-sync");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile long backoffUntilEpochMs = 0;
    private volatile Integer activeUserId = null;

    public void startBackgroundSync() {
        if (!started.compareAndSet(false, true)) return;
        scheduler.scheduleAtFixedRate(this::safeSync, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public void stopBackgroundSync() {
        started.set(false);
        scheduler.shutdownNow();
    }

    private void safeSync() {
        if (!running.compareAndSet(false, true)) return;
        try {
            sync();
            consecutiveFailures.set(0);
            backoffUntilEpochMs = 0;
        } catch (SessionExpiredException e) {
            System.err.println("Sync paused (session expired): " + e.getMessage());
            consecutiveFailures.set(0);
            backoffUntilEpochMs = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
        } catch (Exception e) {
            int n = consecutiveFailures.incrementAndGet();
            long delaySec = Math.min(INTERVAL_SECONDS * (1L << Math.min(n, 6)), MAX_BACKOFF_SECONDS);
            backoffUntilEpochMs = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(delaySec);
            System.err.println("Background sync failed (attempt " + n + ", next retry in "
                    + delaySec + "s): " + e.getMessage());
        } finally {
            running.set(false);
        }
    }

    public void sync() throws Exception {
        if (ApiClient.getToken() == null || ApiClient.getToken().isBlank()) {
            return;
        }
        if (!ApiClient.isTokenUsable()) {
            throw new SessionExpiredException("JWT expired locally");
        }
        if (System.currentTimeMillis() < backoffUntilEpochMs) {
            return;
        }
        ensureActiveUser();
        pushDirtyRows();
        pullSinceCursor();
    }

    /**
     * Call after login. If the user changed, wipe local data so offline rows
     * from account A are never pushed as account B.
     */
    public void onUserLoggedIn(User user) {
        if (user == null) return;
        int uid = user.getUserId();
        Integer previous = activeUserId;
        if (previous != null && previous != uid) {
            DatabaseHelper.wipeLocalData();
            AppSettings.get().clearSyncCursor(previous);
            consecutiveFailures.set(0);
            backoffUntilEpochMs = 0;
        }
        activeUserId = uid;
        AppSettings.get().putIntPref("sync.lastUserId", uid);
    }

    private void ensureActiveUser() {
        if (activeUserId != null) return;
        UserSession session = UserSession.getInstance();
        User u = session.getUser();
        if (u != null) {
            activeUserId = u.getUserId();
            return;
        }
        int last = AppSettings.get().getIntPref("sync.lastUserId", -1);
        if (last > 0) activeUserId = last;
    }

    private int requireUserId() throws SessionExpiredException {
        ensureActiveUser();
        if (activeUserId == null || activeUserId <= 0) {
            throw new SessionExpiredException("No active user for sync");
        }
        return activeUserId;
    }

    // ---- push ---------------------------------------------------------

    private void pushDirtyRows() throws Exception {
        int userId = requireUserId();
        List<Map<String, Object>> lifeAreas = loadPendingLifeAreas();
        List<Map<String, Object>> tasks = loadPendingTasks();
        List<Map<String, Object>> sessions = loadPendingFocusSessions();
        List<Map<String, Object>> exams = loadPendingExams();
        List<Map<String, Object>> grades = loadPendingGrades();
        List<Map<String, Object>> reminders = loadPendingReminders();
        List<Map<String, Object>> settings = loadPendingSettings();

        if (lifeAreas.isEmpty() && tasks.isEmpty() && sessions.isEmpty()
                && exams.isEmpty() && grades.isEmpty() && reminders.isEmpty() && settings.isEmpty()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("protocolVersion", PROTOCOL_VERSION);
        if (!lifeAreas.isEmpty()) payload.put("lifeAreas", lifeAreas);
        if (!tasks.isEmpty()) payload.put("tasks", tasks);
        if (!sessions.isEmpty()) payload.put("focusSessions", sessions);
        if (!exams.isEmpty()) payload.put("exams", exams);
        if (!grades.isEmpty()) payload.put("grades", grades);
        if (!reminders.isEmpty()) payload.put("reminders", reminders);
        if (!settings.isEmpty()) payload.put("settings", settings);

        String json = JsonMapper.get().writeValueAsString(payload);
        HttpRequest request = ApiClient.prepareRequest("/sync/push")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = ApiClient.sendRequest(request);
        if (response.statusCode() == 401) {
            throw new SessionExpiredException("Push unauthorized");
        }
        if (response.statusCode() != 200) {
            throw new Exception("Push failed: HTTP " + response.statusCode());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> body = JsonMapper.get().readValue(response.body(), Map.class);
        Map<String, Integer> idMap = new HashMap<>();
        Object rawMap = body.get("idMap");
        if (rawMap instanceof Map<?, ?> m) {
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    idMap.put(String.valueOf(e.getKey()), ((Number) e.getValue()).intValue());
                }
            }
        }
        Set<String> rejected = rejectedKeys(body);
        if (!rejected.isEmpty()) {
            System.err.println("Server rejected " + rejected.size() + " row(s), keeping them pending: " + rejected);
        }

        applyIdMap(idMap);
        markPushedRowsSynced(lifeAreas, tasks, sessions, exams, grades, reminders, settings, rejected);
    }

    /** Keys from the push response the server refused to write ("task:-3"). Package-visible for tests. */
    static Set<String> rejectedKeys(Map<String, Object> body) {
        Set<String> out = new HashSet<>();
        if (body != null && body.get("rejected") instanceof Map<?, ?> m) {
            for (Object k : m.keySet()) {
                if (k != null) out.add(String.valueOf(k));
            }
        }
        return out;
    }

    private List<Map<String, Object>> loadPendingLifeAreas() throws Exception {
        return loadPending(
                "SELECT lifeAreaId, name, color, active, academicYear, updatedAt, deletedAt, clientUuid " +
                        "FROM LifeAreas WHERE syncStatus = 'PENDING_SYNC'",
                rs -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    int id = rs.getInt("lifeAreaId");
                    row.put("localId", id);
                    if (id > 0) row.put("lifeAreaId", id);
                    row.put("name", rs.getString("name"));
                    row.put("color", rs.getString("color"));
                    row.put("active", rs.getInt("active") != 0);
                    row.put("academicYear", rs.getString("academicYear"));
                    row.put("updatedAt", rs.getString("updatedAt"));
                    row.put("deletedAt", rs.getString("deletedAt"));
                    row.put("clientUuid", rs.getString("clientUuid"));
                    return row;
                });
    }

    private List<Map<String, Object>> loadPendingTasks() throws Exception {
        return loadPending(
                "SELECT taskId, title, description, isCompleted, isHabit, priority, " +
                        "startDate, dueDate, lifeAreaId, projectId, orderIndex, updatedAt, deletedAt, clientUuid " +
                        "FROM Tasks WHERE syncStatus = 'PENDING_SYNC'",
                rs -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    int id = rs.getInt("taskId");
                    row.put("localId", id);
                    if (id > 0) row.put("taskId", id);
                    row.put("title", rs.getString("title"));
                    row.put("description", rs.getString("description"));
                    row.put("isCompleted", rs.getInt("isCompleted") == 1);
                    row.put("isHabit", rs.getInt("isHabit") == 1);
                    row.put("priority", rs.getString("priority"));
                    row.put("startDate", rs.getString("startDate"));
                    row.put("dueDate", rs.getString("dueDate"));
                    row.put("lifeAreaId", getInteger(rs, "lifeAreaId"));
                    row.put("projectId", getInteger(rs, "projectId"));
                    row.put("orderIndex", rs.getInt("orderIndex"));
                    row.put("updatedAt", rs.getString("updatedAt"));
                    row.put("deletedAt", rs.getString("deletedAt"));
                    row.put("clientUuid", rs.getString("clientUuid"));
                    return row;
                });
    }

    private List<Map<String, Object>> loadPendingFocusSessions() throws Exception {
        return loadPending(
                "SELECT sessionId, taskId, startedAt, endedAt, durationSeconds, mode, completed, " +
                        "updatedAt, deletedAt, clientUuid FROM FocusSessions WHERE syncStatus = 'PENDING_SYNC'",
                rs -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    int id = rs.getInt("sessionId");
                    row.put("localId", id);
                    if (id > 0) row.put("sessionId", id);
                    row.put("taskId", getInteger(rs, "taskId"));
                    row.put("startedAt", rs.getString("startedAt"));
                    row.put("endedAt", rs.getString("endedAt"));
                    row.put("durationSeconds", rs.getInt("durationSeconds"));
                    row.put("mode", rs.getString("mode"));
                    row.put("completed", rs.getInt("completed") == 1);
                    row.put("updatedAt", rs.getString("updatedAt"));
                    row.put("deletedAt", rs.getString("deletedAt"));
                    row.put("clientUuid", rs.getString("clientUuid"));
                    return row;
                });
    }

    private List<Map<String, Object>> loadPendingExams() throws Exception {
        return loadPending(
                "SELECT examId, subjectId, nameExam, examType, examDate, classroom, " +
                        "updatedAt, deletedAt, clientUuid FROM Exams WHERE syncStatus = 'PENDING_SYNC'",
                rs -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    int id = rs.getInt("examId");
                    row.put("localId", id);
                    if (id > 0) row.put("examId", id);
                    row.put("lifeAreaId", getInteger(rs, "subjectId"));
                    row.put("nameExam", rs.getString("nameExam"));
                    row.put("examType", rs.getString("examType"));
                    row.put("examDate", rs.getString("examDate"));
                    row.put("classroom", rs.getString("classroom"));
                    row.put("updatedAt", rs.getString("updatedAt"));
                    row.put("deletedAt", rs.getString("deletedAt"));
                    row.put("clientUuid", rs.getString("clientUuid"));
                    return row;
                });
    }

    private List<Map<String, Object>> loadPendingGrades() throws Exception {
        return loadPending(
                "SELECT gradeId, subjectId, concept, score, weight, gradeDate, " +
                        "updatedAt, deletedAt, clientUuid FROM Grades WHERE syncStatus = 'PENDING_SYNC'",
                rs -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    int id = rs.getInt("gradeId");
                    row.put("localId", id);
                    if (id > 0) row.put("gradeId", id);
                    row.put("lifeAreaId", getInteger(rs, "subjectId"));
                    row.put("concept", rs.getString("concept"));
                    row.put("score", rs.getObject("score") == null ? null : rs.getDouble("score"));
                    row.put("weight", rs.getObject("weight") == null ? null : rs.getDouble("weight"));
                    row.put("gradeDate", rs.getString("gradeDate"));
                    row.put("updatedAt", rs.getString("updatedAt"));
                    row.put("deletedAt", rs.getString("deletedAt"));
                    row.put("clientUuid", rs.getString("clientUuid"));
                    return row;
                });
    }

    private List<Map<String, Object>> loadPendingReminders() throws Exception {
        return loadPending(
                "SELECT reminderId, message, dateTime, taskId, examId, " +
                        "updatedAt, deletedAt, clientUuid FROM Reminders WHERE syncStatus = 'PENDING_SYNC'",
                rs -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    int id = rs.getInt("reminderId");
                    row.put("localId", id);
                    if (id > 0) row.put("reminderId", id);
                    row.put("message", rs.getString("message"));
                    row.put("dateTime", rs.getString("dateTime"));
                    row.put("taskId", getInteger(rs, "taskId"));
                    row.put("examId", getInteger(rs, "examId"));
                    row.put("updatedAt", rs.getString("updatedAt"));
                    row.put("deletedAt", rs.getString("deletedAt"));
                    row.put("clientUuid", rs.getString("clientUuid"));
                    return row;
                });
    }

    private List<Map<String, Object>> loadPendingSettings() throws Exception {
        return loadPending(
                "SELECT settingKey, settingValue, updatedAt FROM UserSettings WHERE syncStatus = 'PENDING_SYNC'",
                rs -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("settingKey", rs.getString("settingKey"));
                    row.put("settingValue", rs.getString("settingValue"));
                    row.put("updatedAt", rs.getString("updatedAt"));
                    return row;
                });
    }

    @FunctionalInterface
    private interface RowMapper {
        Map<String, Object> map(ResultSet rs) throws SQLException;
    }

    private List<Map<String, Object>> loadPending(String sql, RowMapper mapper) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(mapper.map(rs));
            }
        }
        return rows;
    }

    /**
     * Rename local negative PKs to server ids and fix FK references.
     * Package-visible for unit tests.
     */
    void applyIdMap(Map<String, Integer> idMap) throws SQLException, java.io.IOException {
        if (idMap == null || idMap.isEmpty()) return;
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Map.Entry<String, Integer> entry : idMap.entrySet()) {
                    String[] parts = entry.getKey().split(":", 2);
                    if (parts.length != 2) continue;
                    String type = parts[0];
                    int oldId = Integer.parseInt(parts[1]);
                    int newId = entry.getValue();
                    if (oldId == newId || oldId >= 0) continue;

                    switch (type) {
                        case "lifeArea" -> {
                            remap(conn, "LifeAreas", "lifeAreaId", oldId, newId);
                            remapFk(conn, "Tasks", "lifeAreaId", oldId, newId);
                            remapFk(conn, "Exams", "subjectId", oldId, newId);
                            remapFk(conn, "Grades", "subjectId", oldId, newId);
                        }
                        case "task" -> {
                            remap(conn, "Tasks", "taskId", oldId, newId);
                            remapFk(conn, "FocusSessions", "taskId", oldId, newId);
                            remapFk(conn, "Reminders", "taskId", oldId, newId);
                        }
                        case "focusSession" -> remap(conn, "FocusSessions", "sessionId", oldId, newId);
                        case "exam" -> {
                            remap(conn, "Exams", "examId", oldId, newId);
                            remapFk(conn, "Reminders", "examId", oldId, newId);
                        }
                        case "grade" -> remap(conn, "Grades", "gradeId", oldId, newId);
                        case "reminder" -> remap(conn, "Reminders", "reminderId", oldId, newId);
                        default -> { /* settings have no server id */ }
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static void remap(Connection conn, String table, String pk, int oldId, int newId)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE " + table + " SET " + pk + " = ? WHERE " + pk + " = ?")) {
            ps.setInt(1, newId);
            ps.setInt(2, oldId);
            ps.executeUpdate();
        }
    }

    private static void remapFk(Connection conn, String table, String fk, int oldId, int newId)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE " + table + " SET " + fk + " = ? WHERE " + fk + " = ?")) {
            ps.setInt(1, newId);
            ps.setInt(2, oldId);
            ps.executeUpdate();
        }
    }

    void markPushedRowsSynced(List<Map<String, Object>> lifeAreas,
                              List<Map<String, Object>> tasks,
                              List<Map<String, Object>> sessions,
                              List<Map<String, Object>> exams,
                              List<Map<String, Object>> grades,
                              List<Map<String, Object>> reminders,
                              List<Map<String, Object>> settings,
                              Set<String> rejected) throws Exception {
        Set<String> rej = rejected == null ? Set.of() : rejected;
        try (Connection conn = DatabaseHelper.getConnection()) {
            markSynced(conn, "LifeAreas", "lifeAreaId", "lifeArea", lifeAreas, "localId", rej);
            markSynced(conn, "Tasks", "taskId", "task", tasks, "localId", rej);
            markSynced(conn, "FocusSessions", "sessionId", "focusSession", sessions, "localId", rej);
            markSynced(conn, "Exams", "examId", "exam", exams, "localId", rej);
            markSynced(conn, "Grades", "gradeId", "grade", grades, "localId", rej);
            markSynced(conn, "Reminders", "reminderId", "reminder", reminders, "localId", rej);
            for (Map<String, Object> row : settings) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE UserSettings SET syncStatus = 'SYNCED' WHERE settingKey = ?")) {
                    ps.setString(1, (String) row.get("settingKey"));
                    ps.executeUpdate();
                }
            }
        }
    }

    private static void markSynced(Connection conn, String table, String pk, String type,
                                   List<Map<String, Object>> rows, String localIdKey,
                                   Set<String> rejected) throws SQLException {
        if (rows == null) return;
        for (Map<String, Object> row : rows) {
            Object localId = row.get(localIdKey);
            if (localId == null) continue;
            if (rejected.contains(type + ":" + localId)) continue;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE " + table + " SET syncStatus = 'SYNCED' WHERE " + pk + " = ?")) {
                ps.setInt(1, ((Number) localId).intValue());
                ps.executeUpdate();
            }
        }
    }

    // ---- pull ---------------------------------------------------------

    private void pullSinceCursor() throws Exception {
        int userId = requireUserId();
        String cursor = AppSettings.get().getSyncCursor(userId);
        String url = cursor == null || cursor.isEmpty()
                ? "/sync/pull"
                : "/sync/pull?since=" + java.net.URLEncoder.encode(cursor, java.nio.charset.StandardCharsets.UTF_8);

        HttpRequest request = ApiClient.prepareRequest(url).GET().build();
        HttpResponse<String> response = ApiClient.sendRequest(request);
        if (response.statusCode() == 401) {
            throw new SessionExpiredException("Pull unauthorized");
        }
        if (response.statusCode() != 200) {
            throw new Exception("Pull failed: HTTP " + response.statusCode());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> body = JsonMapper.get().readValue(response.body(), Map.class);

        Object proto = body.get("protocolVersion");
        if (proto instanceof Number n && n.intValue() > PROTOCOL_VERSION) {
            System.err.println("Server protocol " + n + " is newer than client " + PROTOCOL_VERSION
                    + " — update the desktop app when available.");
        }

        mergeLifeAreas(asList(body.get("lifeAreas")));
        mergeTasks(asList(body.get("tasks")));
        mergeFocusSessions(asList(body.get("focusSessions")));
        mergeExams(asList(body.get("exams")));
        mergeGrades(asList(body.get("grades")));
        mergeReminders(asList(body.get("reminders")));
        mergeSettings(asList(body.get("settings")));

        Object serverTime = body.get("serverTime");
        if (serverTime != null) {
            AppSettings.get().setSyncCursor(userId, String.valueOf(serverTime));
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asList(Object value) {
        if (value == null) return List.of();
        if (value instanceof List<?> list) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) {
                    out.add((Map<String, Object>) m);
                }
            }
            return out;
        }
        return List.of();
    }

    private void mergeLifeAreas(List<Map<String, Object>> remoteRows) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            for (Map<String, Object> row : remoteRows) {
                Integer serverId = intOrNull(row.get("lifeAreaId"));
                if (serverId == null) continue;
                if (shouldApplyRemote(conn, "LifeAreas", "lifeAreaId", serverId, "LifeArea", "name",
                        strOrNull(row.get("updatedAt")), strOrNull(row.get("name")))) {
                    upsertLifeArea(conn, serverId, row);
                }
            }
        }
    }

    void mergeTasks(List<Map<String, Object>> remoteRows) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            for (Map<String, Object> row : remoteRows) {
                Integer serverId = intOrNull(row.get("taskId"));
                if (serverId == null) continue;
                if (shouldApplyRemote(conn, "Tasks", "taskId", serverId, "Task", "title",
                        strOrNull(row.get("updatedAt")), strOrNull(row.get("title")))) {
                    upsertTask(conn, serverId, row);
                }
            }
        }
    }

    private void mergeFocusSessions(List<Map<String, Object>> remoteRows) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            for (Map<String, Object> row : remoteRows) {
                Integer serverId = intOrNull(row.get("sessionId"));
                if (serverId == null) continue;
                if (shouldApplyRemote(conn, "FocusSessions", "sessionId", serverId, "FocusSession", "startedAt",
                        strOrNull(row.get("updatedAt")), strOrNull(row.get("startedAt")))) {
                    upsertFocusSession(conn, serverId, row);
                }
            }
        }
    }

    private void mergeExams(List<Map<String, Object>> remoteRows) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            for (Map<String, Object> row : remoteRows) {
                Integer serverId = intOrNull(row.get("examId"));
                if (serverId == null) continue;
                if (shouldApplyRemote(conn, "Exams", "examId", serverId, "Exam", "nameExam",
                        strOrNull(row.get("updatedAt")), strOrNull(row.get("nameExam")))) {
                    upsertExam(conn, serverId, row);
                }
            }
        }
    }

    private void mergeGrades(List<Map<String, Object>> remoteRows) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            for (Map<String, Object> row : remoteRows) {
                Integer serverId = intOrNull(row.get("gradeId"));
                if (serverId == null) continue;
                if (shouldApplyRemote(conn, "Grades", "gradeId", serverId, "Grade", "concept",
                        strOrNull(row.get("updatedAt")), strOrNull(row.get("concept")))) {
                    upsertGrade(conn, serverId, row);
                }
            }
        }
    }

    private void mergeReminders(List<Map<String, Object>> remoteRows) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            for (Map<String, Object> row : remoteRows) {
                Integer serverId = intOrNull(row.get("reminderId"));
                if (serverId == null) continue;
                if (shouldApplyRemote(conn, "Reminders", "reminderId", serverId, "Reminder", "message",
                        strOrNull(row.get("updatedAt")), strOrNull(row.get("message")))) {
                    upsertReminder(conn, serverId, row);
                }
            }
        }
    }

    private void mergeSettings(List<Map<String, Object>> remoteRows) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            for (Map<String, Object> row : remoteRows) {
                String key = strOrNull(row.get("settingKey"));
                if (key == null) continue;
                String remoteUpdated = strOrNull(row.get("updatedAt"));
                if (hasNewerLocalPendingSetting(conn, key, remoteUpdated)) {
                    continue;
                }
                upsertSetting(conn, key, row);
                applySettingToAppSettings(key, strOrNull(row.get("settingValue")));
            }
        }
    }

    /** True when local row is still PENDING_SYNC and strictly newer than remote (local wins). */
    static boolean isLocalNewer(String localUpdated, String localStatus, String remoteUpdated) {
        if (localUpdated == null || remoteUpdated == null) return false;
        if (!"PENDING_SYNC".equals(localStatus)) return false;
        try {
            return LocalDateTime.parse(localUpdated).isAfter(LocalDateTime.parse(remoteUpdated));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Decide whether the remote row should overwrite the local copy.
     * Local wins only if PENDING_SYNC and strictly newer (LWW). When a
     * PENDING_SYNC local edit loses to a newer remote row, it is recorded as a
     * conflict so the overwrite is visible instead of silent.
     * Package-visible for unit tests.
     */
    boolean shouldApplyRemote(Connection conn, String table, String pk, int id,
                              String entityType, String summaryColumn,
                              String remoteUpdated, String remoteSummary) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT updatedAt, syncStatus, " + summaryColumn + " FROM " + table + " WHERE " + pk + " = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return true;
                String localUpdated = rs.getString("updatedAt");
                String localStatus = rs.getString("syncStatus");
                if (isLocalNewer(localUpdated, localStatus, remoteUpdated)) {
                    return false;
                }
                if ("PENDING_SYNC".equals(localStatus)) {
                    SyncConflictService.record(conn, entityType, id,
                            rs.getString(summaryColumn), remoteSummary, localUpdated, remoteUpdated);
                }
                return true;
            }
        }
    }

    private boolean hasNewerLocalPendingSetting(Connection conn, String key, String remoteUpdated)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT updatedAt, syncStatus FROM UserSettings WHERE settingKey = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                return isLocalNewer(rs.getString("updatedAt"), rs.getString("syncStatus"), remoteUpdated);
            }
        }
    }

    private void upsertLifeArea(Connection conn, int serverId, Map<String, Object> row) throws SQLException {
        boolean exists = rowExists(conn, "LifeAreas", "lifeAreaId", serverId);
        if (exists) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE LifeAreas SET name = ?, color = ?, active = ?, academicYear = ?, " +
                            "updatedAt = ?, deletedAt = ?, syncStatus = 'SYNCED' WHERE lifeAreaId = ?")) {
                ps.setString(1, strOrNull(row.get("name")));
                ps.setString(2, strOrNull(row.get("color")));
                ps.setBoolean(3, boolOrTrue(row.get("active")));
                ps.setString(4, strOrNull(row.get("academicYear")));
                ps.setString(5, strOrNull(row.get("updatedAt")));
                ps.setString(6, strOrNull(row.get("deletedAt")));
                ps.setInt(7, serverId);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO LifeAreas (lifeAreaId, name, color, active, academicYear, updatedAt, deletedAt, syncStatus, clientUuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, 'SYNCED', ?)")) {
                ps.setInt(1, serverId);
                ps.setString(2, strOrNull(row.get("name")));
                ps.setString(3, strOrNull(row.get("color")));
                ps.setBoolean(4, boolOrTrue(row.get("active")));
                ps.setString(5, strOrNull(row.get("academicYear")));
                ps.setString(6, strOrNull(row.get("updatedAt")));
                ps.setString(7, strOrNull(row.get("deletedAt")));
                ps.setString(8, strOrNull(row.get("clientUuid")));
                ps.executeUpdate();
            }
        }
    }

    private void upsertTask(Connection conn, int serverId, Map<String, Object> row) throws SQLException {
        Integer lifeAreaId = intOrNull(row.get("lifeAreaId"));
        Integer projectId = intOrNull(row.get("projectId"));
        boolean exists = rowExists(conn, "Tasks", "taskId", serverId);
        if (exists) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Tasks SET title = ?, description = ?, isCompleted = ?, isHabit = ?, " +
                            "priority = ?, startDate = ?, dueDate = ?, lifeAreaId = ?, projectId = ?, " +
                            "orderIndex = ?, updatedAt = ?, deletedAt = ?, syncStatus = 'SYNCED' WHERE taskId = ?")) {
                int i = 1;
                ps.setString(i++, strOrNull(row.get("title")));
                ps.setString(i++, strOrNull(row.get("description")));
                ps.setBoolean(i++, boolOrFalse(row.get("isCompleted")));
                ps.setBoolean(i++, boolOrFalse(row.get("isHabit")));
                ps.setString(i++, strOrNull(row.get("priority")));
                ps.setString(i++, strOrNull(row.get("startDate")));
                ps.setString(i++, strOrNull(row.get("dueDate")));
                if (lifeAreaId == null) ps.setObject(i++, null); else ps.setInt(i++, lifeAreaId);
                if (projectId == null) ps.setObject(i++, null); else ps.setInt(i++, projectId);
                ps.setInt(i++, intOrZero(row.get("orderIndex")));
                ps.setString(i++, strOrNull(row.get("updatedAt")));
                ps.setString(i++, strOrNull(row.get("deletedAt")));
                ps.setInt(i, serverId);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Tasks (taskId, title, description, isCompleted, isHabit, priority, " +
                            "startDate, dueDate, lifeAreaId, projectId, orderIndex, updatedAt, deletedAt, syncStatus, clientUuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'SYNCED', ?)")) {
                int i = 1;
                ps.setInt(i++, serverId);
                ps.setString(i++, strOrNull(row.get("title")));
                ps.setString(i++, strOrNull(row.get("description")));
                ps.setBoolean(i++, boolOrFalse(row.get("isCompleted")));
                ps.setBoolean(i++, boolOrFalse(row.get("isHabit")));
                ps.setString(i++, strOrNull(row.get("priority")));
                ps.setString(i++, strOrNull(row.get("startDate")));
                ps.setString(i++, strOrNull(row.get("dueDate")));
                if (lifeAreaId == null) ps.setObject(i++, null); else ps.setInt(i++, lifeAreaId);
                if (projectId == null) ps.setObject(i++, null); else ps.setInt(i++, projectId);
                ps.setInt(i++, intOrZero(row.get("orderIndex")));
                ps.setString(i++, strOrNull(row.get("updatedAt")));
                ps.setString(i++, strOrNull(row.get("deletedAt")));
                ps.setString(i++, strOrNull(row.get("clientUuid")));
                ps.executeUpdate();
            }
        }
    }

    private void upsertFocusSession(Connection conn, int serverId, Map<String, Object> row) throws SQLException {
        Integer taskId = intOrNull(row.get("taskId"));
        boolean exists = rowExists(conn, "FocusSessions", "sessionId", serverId);
        if (exists) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE FocusSessions SET taskId = ?, startedAt = ?, endedAt = ?, durationSeconds = ?, " +
                            "mode = ?, completed = ?, updatedAt = ?, deletedAt = ?, syncStatus = 'SYNCED' " +
                            "WHERE sessionId = ?")) {
                int i = 1;
                if (taskId == null) ps.setObject(i++, null); else ps.setInt(i++, taskId);
                ps.setString(i++, strOrNull(row.get("startedAt")));
                ps.setString(i++, strOrNull(row.get("endedAt")));
                ps.setInt(i++, intOrZero(row.get("durationSeconds")));
                ps.setString(i++, strOrNull(row.get("mode")));
                ps.setBoolean(i++, boolOrTrue(row.get("completed")));
                ps.setString(i++, strOrNull(row.get("updatedAt")));
                ps.setString(i++, strOrNull(row.get("deletedAt")));
                ps.setInt(i, serverId);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO FocusSessions (sessionId, taskId, startedAt, endedAt, durationSeconds, " +
                            "mode, completed, updatedAt, deletedAt, syncStatus, clientUuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'SYNCED', ?)")) {
                int i = 1;
                ps.setInt(i++, serverId);
                if (taskId == null) ps.setObject(i++, null); else ps.setInt(i++, taskId);
                ps.setString(i++, strOrNull(row.get("startedAt")));
                ps.setString(i++, strOrNull(row.get("endedAt")));
                ps.setInt(i++, intOrZero(row.get("durationSeconds")));
                ps.setString(i++, strOrNull(row.get("mode")));
                ps.setBoolean(i++, boolOrTrue(row.get("completed")));
                ps.setString(i++, strOrNull(row.get("updatedAt")));
                ps.setString(i++, strOrNull(row.get("deletedAt")));
                ps.setString(i++, strOrNull(row.get("clientUuid")));
                ps.executeUpdate();
            }
        }
    }

    private void upsertExam(Connection conn, int serverId, Map<String, Object> row) throws SQLException {
        Integer lifeAreaId = intOrNull(row.get("lifeAreaId"));
        boolean exists = rowExists(conn, "Exams", "examId", serverId);
        if (exists) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Exams SET subjectId = ?, nameExam = ?, examType = ?, examDate = ?, " +
                            "classroom = ?, updatedAt = ?, deletedAt = ?, syncStatus = 'SYNCED' WHERE examId = ?")) {
                int i = 1;
                if (lifeAreaId == null) ps.setObject(i++, null); else ps.setInt(i++, lifeAreaId);
                ps.setString(i++, strOrNull(row.get("nameExam")));
                ps.setString(i++, strOrNull(row.get("examType")));
                ps.setString(i++, strOrNull(row.get("examDate")));
                ps.setString(i++, strOrNull(row.get("classroom")));
                ps.setString(i++, strOrNull(row.get("updatedAt")));
                ps.setString(i++, strOrNull(row.get("deletedAt")));
                ps.setInt(i, serverId);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Exams (examId, subjectId, nameExam, examType, examDate, classroom, " +
                            "updatedAt, deletedAt, syncStatus, clientUuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'SYNCED', ?)")) {
                int i = 1;
                ps.setInt(i++, serverId);
                if (lifeAreaId == null) ps.setObject(i++, null); else ps.setInt(i++, lifeAreaId);
                ps.setString(i++, strOrNull(row.get("nameExam")));
                ps.setString(i++, strOrNull(row.get("examType")));
                ps.setString(i++, strOrNull(row.get("examDate")));
                ps.setString(i++, strOrNull(row.get("classroom")));
                ps.setString(i++, strOrNull(row.get("updatedAt")));
                ps.setString(i++, strOrNull(row.get("deletedAt")));
                ps.setString(i++, strOrNull(row.get("clientUuid")));
                ps.executeUpdate();
            }
        }
    }

    private void upsertGrade(Connection conn, int serverId, Map<String, Object> row) throws SQLException {
        Integer lifeAreaId = intOrNull(row.get("lifeAreaId"));
        boolean exists = rowExists(conn, "Grades", "gradeId", serverId);
        if (exists) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Grades SET subjectId = ?, concept = ?, score = ?, weight = ?, gradeDate = ?, " +
                            "updatedAt = ?, deletedAt = ?, syncStatus = 'SYNCED' WHERE gradeId = ?")) {
                int i = 1;
                if (lifeAreaId == null) ps.setObject(i++, null); else ps.setInt(i++, lifeAreaId);
                ps.setString(i++, strOrNull(row.get("concept")));
                setObjectOrDouble(ps, i++, row.get("score"));
                setObjectOrDouble(ps, i++, row.get("weight"));
                ps.setString(i++, strOrNull(row.get("gradeDate")));
                ps.setString(i++, strOrNull(row.get("updatedAt")));
                ps.setString(i++, strOrNull(row.get("deletedAt")));
                ps.setInt(i, serverId);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Grades (gradeId, subjectId, concept, score, weight, gradeDate, " +
                            "updatedAt, deletedAt, syncStatus, clientUuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'SYNCED', ?)")) {
                int i = 1;
                ps.setInt(i++, serverId);
                if (lifeAreaId == null) ps.setObject(i++, null); else ps.setInt(i++, lifeAreaId);
                ps.setString(i++, strOrNull(row.get("concept")));
                setObjectOrDouble(ps, i++, row.get("score"));
                setObjectOrDouble(ps, i++, row.get("weight"));
                ps.setString(i++, strOrNull(row.get("gradeDate")));
                ps.setString(i++, strOrNull(row.get("updatedAt")));
                ps.setString(i++, strOrNull(row.get("deletedAt")));
                ps.setString(i++, strOrNull(row.get("clientUuid")));
                ps.executeUpdate();
            }
        }
    }

    private void upsertReminder(Connection conn, int serverId, Map<String, Object> row) throws SQLException {
        Integer taskId = intOrNull(row.get("taskId"));
        Integer examId = intOrNull(row.get("examId"));
        boolean exists = rowExists(conn, "Reminders", "reminderId", serverId);
        if (exists) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Reminders SET message = ?, dateTime = ?, taskId = ?, examId = ?, " +
                            "updatedAt = ?, deletedAt = ?, syncStatus = 'SYNCED' WHERE reminderId = ?")) {
                int i = 1;
                ps.setString(i++, strOrNull(row.get("message")));
                ps.setString(i++, strOrNull(row.get("dateTime")));
                if (taskId == null) ps.setObject(i++, null); else ps.setInt(i++, taskId);
                if (examId == null) ps.setObject(i++, null); else ps.setInt(i++, examId);
                ps.setString(i++, strOrNull(row.get("updatedAt")));
                ps.setString(i++, strOrNull(row.get("deletedAt")));
                ps.setInt(i, serverId);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Reminders (reminderId, message, dateTime, taskId, examId, " +
                            "updatedAt, deletedAt, syncStatus, clientUuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, 'SYNCED', ?)")) {
                int i = 1;
                ps.setInt(i++, serverId);
                ps.setString(i++, strOrNull(row.get("message")));
                ps.setString(i++, strOrNull(row.get("dateTime")));
                if (taskId == null) ps.setObject(i++, null); else ps.setInt(i++, taskId);
                if (examId == null) ps.setObject(i++, null); else ps.setInt(i++, examId);
                ps.setString(i++, strOrNull(row.get("updatedAt")));
                ps.setString(i++, strOrNull(row.get("deletedAt")));
                ps.setString(i++, strOrNull(row.get("clientUuid")));
                ps.executeUpdate();
            }
        }
    }

    private void upsertSetting(Connection conn, String key, Map<String, Object> row) throws SQLException {
        String value = strOrNull(row.get("settingValue"));
        String updatedAt = strOrNull(row.get("updatedAt"));
        boolean exists = rowExists(conn, "UserSettings", "settingKey", key);
        if (exists) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE UserSettings SET settingValue = ?, updatedAt = ?, syncStatus = 'SYNCED' WHERE settingKey = ?")) {
                ps.setString(1, value);
                ps.setString(2, updatedAt);
                ps.setString(3, key);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO UserSettings (settingKey, settingValue, updatedAt, syncStatus) " +
                            "VALUES (?, ?, ?, 'SYNCED')")) {
                ps.setString(1, key);
                ps.setString(2, value);
                ps.setString(3, updatedAt);
                ps.executeUpdate();
            }
        }
    }

    /** Device-local keys must never be overwritten from the cloud. */
    private void applySettingToAppSettings(String key, String value) {
        if (key == null || value == null) return;
        AppSettings s = AppSettings.get();
        switch (key) {
            case "ui.theme" -> s.setTheme(value);
            case "timer.preset" -> {
                try {
                    s.setTimerPreset(AppSettings.TimerPreset.valueOf(value));
                } catch (IllegalArgumentException ignored) { /* bad remote value */ }
            }
            case "timer.focus" -> s.setFocusMinutes(parseIntOr(value, s.getFocusMinutes()));
            case "timer.short" -> s.setShortBreakMinutes(parseIntOr(value, s.getShortBreakMinutes()));
            case "timer.long" -> s.setLongBreakMinutes(parseIntOr(value, s.getLongBreakMinutes()));
            case "timer.cycles" -> s.setCyclesBeforeLongBreak(parseIntOr(value, s.getCyclesBeforeLongBreak()));
            case "timer.autostart.breaks" -> s.setAutoStartBreaks(Boolean.parseBoolean(value));
            case "timer.autostart.focus" -> s.setAutoStartFocus(Boolean.parseBoolean(value));
            case "timer.sound" -> s.setSoundEnabled(Boolean.parseBoolean(value));
            default -> { /* ignore unknown or device-local keys */ }
        }
    }

    // ---- helpers ------------------------------------------------------

    private static void setObjectOrDouble(PreparedStatement ps, int idx, Object value) throws SQLException {
        if (value == null) {
            ps.setObject(idx, null);
        } else if (value instanceof Number n) {
            ps.setDouble(idx, n.doubleValue());
        } else {
            try {
                ps.setDouble(idx, Double.parseDouble(String.valueOf(value)));
            } catch (NumberFormatException e) {
                ps.setObject(idx, null);
            }
        }
    }

    private static boolean rowExists(Connection conn, String table, String pk, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM " + table + " WHERE " + pk + " = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean rowExists(Connection conn, String table, String pk, String key) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM " + table + " WHERE " + pk + " = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static Integer getInteger(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    private static Integer intOrNull(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int intOrZero(Object o) {
        Integer v = intOrNull(o);
        return v == null ? 0 : v;
    }

    private static int parseIntOr(String v, int fallback) {
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String strOrNull(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o);
        return s.isEmpty() || "null".equals(s) ? null : s;
    }

    private static boolean boolOrTrue(Object o) {
        if (o == null) return true;
        if (o instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(o));
    }

    private static boolean boolOrFalse(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(o));
    }

    /** Raised when the JWT is missing/expired — pauses sync instead of spinning. */
    static class SessionExpiredException extends Exception {
        SessionExpiredException(String message) {
            super(message);
        }
    }
}
