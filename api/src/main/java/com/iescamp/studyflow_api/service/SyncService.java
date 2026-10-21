package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.dto.SyncPayloadDTO;
import com.iescamp.studyflow_api.dto.SyncPushResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Last-write-wins sync with tombstones + clientUuid idempotency.
 * Uses JdbcTemplate (not JPA) so soft-deleted rows bypass @SQLRestriction
 * and client updatedAt is preserved (entity @PreUpdate would clobber it).
 */
@Service
@RequiredArgsConstructor
public class SyncService {

    private final JdbcTemplate jdbc;

    /** Full pull when since is null (first login / cursor reset). */
    private static final LocalDateTime FULL_SYNC_SINCE = LocalDateTime.of(2000, 1, 1, 0, 0);

    public SyncPayloadDTO pullSince(Integer userId, LocalDateTime since) {
        LocalDateTime from = since == null ? FULL_SYNC_SINCE : since;
        LocalDateTime serverTime = LocalDateTime.now();
        SyncPayloadDTO payload = new SyncPayloadDTO();
        payload.setServerTime(serverTime);

        payload.setLifeAreas(jdbc.query(
                "SELECT lifeAreaId, name, color, active, academicYear, updatedAt, deletedAt, clientUuid " +
                        "FROM LifeAreas WHERE userId = ? AND (updatedAt > ? OR updatedAt IS NULL)",
                (rs, i) -> {
                    SyncPayloadDTO.LifeAreaItem item = new SyncPayloadDTO.LifeAreaItem();
                    item.setLifeAreaId(rs.getInt("lifeAreaId"));
                    item.setName(rs.getString("name"));
                    item.setColor(rs.getString("color"));
                    item.setActive(rs.getObject("active") == null || rs.getBoolean("active"));
                    item.setAcademicYear(rs.getString("academicYear"));
                    item.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
                    item.setDeletedAt(toLocalDateTime(rs.getTimestamp("deletedAt")));
                    item.setClientUuid(rs.getString("clientUuid"));
                    return item;
                },
                userId, Timestamp.valueOf(from)));

        payload.setTasks(jdbc.query(
                "SELECT taskId, lifeAreaId, projectId, title, descriptionTask, priority, " +
                        "isCompleted, isHabit, orderIndex, startDate, dueDate, updatedAt, deletedAt, clientUuid " +
                        "FROM Tasks WHERE userId = ? AND (updatedAt > ? OR updatedAt IS NULL)",
                (rs, i) -> {
                    SyncPayloadDTO.TaskItem item = new SyncPayloadDTO.TaskItem();
                    item.setTaskId(rs.getInt("taskId"));
                    item.setLifeAreaId(getIntOrNull(rs, "lifeAreaId"));
                    item.setProjectId(getIntOrNull(rs, "projectId"));
                    item.setTitle(rs.getString("title"));
                    item.setDescription(rs.getString("descriptionTask"));
                    item.setPriority(rs.getString("priority"));
                    item.setIsCompleted(rs.getBoolean("isCompleted"));
                    item.setIsHabit(rs.getBoolean("isHabit"));
                    item.setOrderIndex(rs.getInt("orderIndex"));
                    item.setStartDate(rs.getString("startDate"));
                    item.setDueDate(rs.getString("dueDate"));
                    item.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
                    item.setDeletedAt(toLocalDateTime(rs.getTimestamp("deletedAt")));
                    item.setClientUuid(rs.getString("clientUuid"));
                    return item;
                },
                userId, Timestamp.valueOf(from)));

        payload.setFocusSessions(jdbc.query(
                "SELECT sessionId, taskId, startedAt, endedAt, durationSeconds, mode, completed, " +
                        "updatedAt, deletedAt, clientUuid " +
                        "FROM FocusSessions WHERE userId = ? AND (updatedAt > ? OR updatedAt IS NULL)",
                (rs, i) -> {
                    SyncPayloadDTO.FocusSessionItem item = new SyncPayloadDTO.FocusSessionItem();
                    item.setSessionId(rs.getInt("sessionId"));
                    item.setTaskId(getIntOrNull(rs, "taskId"));
                    item.setStartedAt(rs.getString("startedAt"));
                    item.setEndedAt(rs.getString("endedAt"));
                    item.setDurationSeconds(rs.getInt("durationSeconds"));
                    item.setMode(rs.getString("mode"));
                    item.setCompleted(rs.getBoolean("completed"));
                    item.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
                    item.setDeletedAt(toLocalDateTime(rs.getTimestamp("deletedAt")));
                    item.setClientUuid(rs.getString("clientUuid"));
                    return item;
                },
                userId, Timestamp.valueOf(from)));

        payload.setExams(jdbc.query(
                "SELECT examId, lifeAreaId, nameExam, examType, examDate, classroom, " +
                        "updatedAt, deletedAt, clientUuid " +
                        "FROM Exams WHERE userId = ? AND (updatedAt > ? OR updatedAt IS NULL)",
                (rs, i) -> {
                    SyncPayloadDTO.ExamItem item = new SyncPayloadDTO.ExamItem();
                    item.setExamId(rs.getInt("examId"));
                    item.setLifeAreaId(getIntOrNull(rs, "lifeAreaId"));
                    item.setNameExam(rs.getString("nameExam"));
                    item.setExamType(rs.getString("examType"));
                    item.setExamDate(rs.getString("examDate"));
                    item.setClassroom(rs.getString("classroom"));
                    item.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
                    item.setDeletedAt(toLocalDateTime(rs.getTimestamp("deletedAt")));
                    item.setClientUuid(rs.getString("clientUuid"));
                    return item;
                },
                userId, Timestamp.valueOf(from)));

        payload.setGrades(jdbc.query(
                "SELECT gradeId, lifeAreaId, concept, score, weight, gradeDate, " +
                        "updatedAt, deletedAt, clientUuid " +
                        "FROM Grades WHERE userId = ? AND (updatedAt > ? OR updatedAt IS NULL)",
                (rs, i) -> {
                    SyncPayloadDTO.GradeItem item = new SyncPayloadDTO.GradeItem();
                    item.setGradeId(rs.getInt("gradeId"));
                    item.setLifeAreaId(getIntOrNull(rs, "lifeAreaId"));
                    item.setConcept(rs.getString("concept"));
                    item.setScore(rs.getObject("score") == null ? null : rs.getDouble("score"));
                    item.setWeight(rs.getObject("weight") == null ? null : rs.getDouble("weight"));
                    item.setGradeDate(rs.getString("gradeDate"));
                    item.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
                    item.setDeletedAt(toLocalDateTime(rs.getTimestamp("deletedAt")));
                    item.setClientUuid(rs.getString("clientUuid"));
                    return item;
                },
                userId, Timestamp.valueOf(from)));

        payload.setReminders(jdbc.query(
                "SELECT reminderId, message, date_Time, taskId, examId, " +
                        "updatedAt, deletedAt, clientUuid " +
                        "FROM Reminders WHERE userId = ? AND (updatedAt > ? OR updatedAt IS NULL)",
                (rs, i) -> {
                    SyncPayloadDTO.ReminderItem item = new SyncPayloadDTO.ReminderItem();
                    item.setReminderId(rs.getInt("reminderId"));
                    item.setMessage(rs.getString("message"));
                    Timestamp dt = rs.getTimestamp("date_Time");
                    item.setDateTime(dt == null ? null : dt.toLocalDateTime().toString());
                    item.setTaskId(getIntOrNull(rs, "taskId"));
                    item.setExamId(getIntOrNull(rs, "examId"));
                    item.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
                    item.setDeletedAt(toLocalDateTime(rs.getTimestamp("deletedAt")));
                    item.setClientUuid(rs.getString("clientUuid"));
                    return item;
                },
                userId, Timestamp.valueOf(from)));

        payload.setSettings(jdbc.query(
                "SELECT settingKey, settingValue, updatedAt FROM UserSettings " +
                        "WHERE userId = ? AND (updatedAt > ? OR updatedAt IS NULL)",
                (rs, i) -> {
                    SyncPayloadDTO.SettingItem item = new SyncPayloadDTO.SettingItem();
                    item.setSettingKey(rs.getString("settingKey"));
                    item.setSettingValue(rs.getString("settingValue"));
                    item.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updatedAt")));
                    return item;
                },
                userId, Timestamp.valueOf(from)));

        return payload;
    }

    @Transactional
    public SyncPushResponseDTO pushBatch(Integer userId, SyncPayloadDTO payload) {
        LocalDateTime now = LocalDateTime.now();
        SyncPushResponseDTO response = new SyncPushResponseDTO();
        response.setServerTime(now);

        Map<Integer, Integer> lifeAreaRemap = new HashMap<>();
        Map<Integer, Integer> taskRemap = new HashMap<>();
        Map<Integer, Integer> examRemap = new HashMap<>();

        if (payload.getLifeAreas() != null) {
            for (SyncPayloadDTO.LifeAreaItem item : payload.getLifeAreas()) {
                if (item.getName() == null || item.getName().isBlank()) {
                    reject(response, "lifeArea", item.getLocalId(), item.getLifeAreaId(), "name required");
                    continue;
                }
                int serverId = upsertLifeArea(userId, item, now, response);
                Integer localKey = item.getLocalId() != null ? item.getLocalId() : item.getLifeAreaId();
                if (localKey != null) {
                    lifeAreaRemap.put(localKey, serverId);
                }
            }
        }

        if (payload.getTasks() != null) {
            for (SyncPayloadDTO.TaskItem item : payload.getTasks()) {
                if (item.getLifeAreaId() != null && item.getLifeAreaId() <= 0) {
                    item.setLifeAreaId(lifeAreaRemap.getOrDefault(item.getLifeAreaId(), null));
                }
                if (item.getLifeAreaId() != null && item.getLifeAreaId() > 0
                        && !ownsLifeArea(userId, item.getLifeAreaId())) {
                    item.setLifeAreaId(null);
                }
                if (item.getTitle() == null || item.getTitle().isBlank()) {
                    reject(response, "task", item.getLocalId(), item.getTaskId(), "title required");
                    continue;
                }
                int serverId = upsertTask(userId, item, now, response);
                Integer localKey = item.getLocalId() != null ? item.getLocalId() : item.getTaskId();
                if (localKey != null) {
                    taskRemap.put(localKey, serverId);
                }
            }
        }

        if (payload.getFocusSessions() != null) {
            for (SyncPayloadDTO.FocusSessionItem item : payload.getFocusSessions()) {
                if (item.getTaskId() != null && item.getTaskId() <= 0) {
                    item.setTaskId(taskRemap.getOrDefault(item.getTaskId(), null));
                }
                if (item.getTaskId() != null && item.getTaskId() > 0
                        && !ownsTask(userId, item.getTaskId())) {
                    item.setTaskId(null);
                }
                if (item.getStartedAt() == null) {
                    reject(response, "focusSession", item.getLocalId(), item.getSessionId(), "startedAt required");
                    continue;
                }
                if (item.getEndedAt() == null) {
                    reject(response, "focusSession", item.getLocalId(), item.getSessionId(),
                            "endedAt required (finish the session before it syncs)");
                    continue;
                }
                upsertFocusSession(userId, item, now, response);
            }
        }

        if (payload.getExams() != null) {
            for (SyncPayloadDTO.ExamItem item : payload.getExams()) {
                if (item.getLifeAreaId() != null && item.getLifeAreaId() <= 0) {
                    item.setLifeAreaId(lifeAreaRemap.getOrDefault(item.getLifeAreaId(), null));
                }
                if (item.getLifeAreaId() != null && item.getLifeAreaId() > 0
                        && !ownsLifeArea(userId, item.getLifeAreaId())) {
                    item.setLifeAreaId(null);
                }
                if (item.getLifeAreaId() == null) {
                    reject(response, "exam", item.getLocalId(), item.getExamId(), "lifeArea required");
                    continue;
                }
                int serverId = upsertExam(userId, item, now, response);
                Integer localKey = item.getLocalId() != null ? item.getLocalId() : item.getExamId();
                if (localKey != null) {
                    examRemap.put(localKey, serverId);
                }
            }
        }

        if (payload.getGrades() != null) {
            for (SyncPayloadDTO.GradeItem item : payload.getGrades()) {
                if (item.getLifeAreaId() != null && item.getLifeAreaId() <= 0) {
                    item.setLifeAreaId(lifeAreaRemap.getOrDefault(item.getLifeAreaId(), null));
                }
                if (item.getLifeAreaId() != null && item.getLifeAreaId() > 0
                        && !ownsLifeArea(userId, item.getLifeAreaId())) {
                    item.setLifeAreaId(null);
                }
                if (item.getLifeAreaId() == null) {
                    reject(response, "grade", item.getLocalId(), item.getGradeId(), "lifeArea required");
                    continue;
                }
                upsertGrade(userId, item, now, response);
            }
        }

        if (payload.getReminders() != null) {
            for (SyncPayloadDTO.ReminderItem item : payload.getReminders()) {
                if (item.getTaskId() != null && item.getTaskId() <= 0) {
                    item.setTaskId(taskRemap.getOrDefault(item.getTaskId(), null));
                }
                if (item.getExamId() != null && item.getExamId() <= 0) {
                    item.setExamId(examRemap.getOrDefault(item.getExamId(), null));
                }
                if (item.getTaskId() != null && item.getTaskId() > 0
                        && !ownsTask(userId, item.getTaskId())) {
                    item.setTaskId(null);
                }
                if (item.getExamId() != null && item.getExamId() > 0
                        && !ownsExam(userId, item.getExamId())) {
                    item.setExamId(null);
                }
                upsertReminder(userId, item, now, response);
            }
        }

        if (payload.getSettings() != null) {
            for (SyncPayloadDTO.SettingItem item : payload.getSettings()) {
                if (item.getSettingKey() == null || item.getSettingKey().isBlank()) continue;
                upsertSetting(userId, item, now);
            }
        }

        return response;
    }

    /** Record a row the server refused; desktop keeps it PENDING_SYNC and retries later. */
    private static void reject(SyncPushResponseDTO response, String type,
                               Integer localId, Integer entityId, String reason) {
        Integer key = localId != null ? localId : entityId;
        response.getRejected().put(type + ":" + (key != null ? key : "?"), reason);
    }

    // ---- life areas ---------------------------------------------------

    private int upsertLifeArea(Integer userId, SyncPayloadDTO.LifeAreaItem item,
                               LocalDateTime now, SyncPushResponseDTO response) {
        LocalDateTime updatedAt = effectiveUpdatedAt(item.getUpdatedAt(), now);
        Integer localKey = item.getLocalId() != null ? item.getLocalId() : item.getLifeAreaId();

        Integer byUuid = findByClientUuid("LifeAreas", "lifeAreaId", userId, item.getClientUuid());
        if (byUuid != null) {
            if (localKey != null) response.getIdMap().put("lifeArea:" + localKey, byUuid);
            return byUuid;
        }

        boolean create = item.getLifeAreaId() == null || item.getLifeAreaId() <= 0;
        if (create) {
            int id = insertLifeArea(userId, item, updatedAt);
            if (localKey != null) response.getIdMap().put("lifeArea:" + localKey, id);
            return id;
        }

        Timestamp existing = jdbc.query(
                "SELECT updatedAt FROM LifeAreas WHERE lifeAreaId = ? AND userId = ?",
                rs -> rs.next() ? rs.getTimestamp(1) : null,
                item.getLifeAreaId(), userId);

        if (existing == null) {
            int id = insertLifeArea(userId, item, updatedAt);
            if (localKey != null) response.getIdMap().put("lifeArea:" + localKey, id);
            return id;
        }

        if (isServerNewer(existing, updatedAt)) {
            if (localKey != null) response.getIdMap().put("lifeArea:" + localKey, item.getLifeAreaId());
            return item.getLifeAreaId();
        }

        jdbc.update(
                "UPDATE LifeAreas SET name = ?, color = ?, active = ?, academicYear = ?, " +
                        "updatedAt = ?, deletedAt = ?, clientUuid = COALESCE(clientUuid, ?) WHERE lifeAreaId = ? AND userId = ?",
                item.getName(), item.getColor(), item.getActive(), item.getAcademicYear(),
                Timestamp.valueOf(updatedAt), toTimestamp(item.getDeletedAt()),
                item.getClientUuid(),
                item.getLifeAreaId(), userId);
        if (localKey != null) response.getIdMap().put("lifeArea:" + localKey, item.getLifeAreaId());
        return item.getLifeAreaId();
    }

    private int insertLifeArea(Integer userId, SyncPayloadDTO.LifeAreaItem item, LocalDateTime updatedAt) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO LifeAreas (userId, name, color, active, academicYear, updatedAt, deletedAt, clientUuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setString(2, item.getName());
            ps.setString(3, item.getColor());
            if (item.getActive() == null) ps.setObject(4, Boolean.TRUE);
            else ps.setBoolean(4, item.getActive());
            ps.setString(5, item.getAcademicYear());
            ps.setTimestamp(6, Timestamp.valueOf(updatedAt));
            ps.setTimestamp(7, toTimestamp(item.getDeletedAt()));
            ps.setString(8, item.getClientUuid());
            return ps;
        }, keys);
        return generatedId(keys);
    }

    // ---- tasks --------------------------------------------------------

    private int upsertTask(Integer userId, SyncPayloadDTO.TaskItem item,
                           LocalDateTime now, SyncPushResponseDTO response) {
        LocalDateTime updatedAt = effectiveUpdatedAt(item.getUpdatedAt(), now);
        Integer localKey = item.getLocalId() != null ? item.getLocalId() : item.getTaskId();

        Integer byUuid = findByClientUuid("Tasks", "taskId", userId, item.getClientUuid());
        if (byUuid != null) {
            if (localKey != null) response.getIdMap().put("task:" + localKey, byUuid);
            return byUuid;
        }

        boolean create = item.getTaskId() == null || item.getTaskId() <= 0;
        if (create) {
            int id = insertTask(userId, item, updatedAt);
            if (localKey != null) response.getIdMap().put("task:" + localKey, id);
            return id;
        }

        Timestamp existing = jdbc.query(
                "SELECT updatedAt FROM Tasks WHERE taskId = ? AND userId = ?",
                rs -> rs.next() ? rs.getTimestamp(1) : null,
                item.getTaskId(), userId);

        if (existing == null) {
            int id = insertTask(userId, item, updatedAt);
            if (localKey != null) response.getIdMap().put("task:" + localKey, id);
            return id;
        }

        if (isServerNewer(existing, updatedAt)) {
            if (localKey != null) response.getIdMap().put("task:" + localKey, item.getTaskId());
            return item.getTaskId();
        }

        jdbc.update(
                "UPDATE Tasks SET title = ?, descriptionTask = ?, priority = ?, isCompleted = ?, " +
                        "isHabit = ?, orderIndex = ?, startDate = ?, dueDate = ?, lifeAreaId = ?, " +
                        "projectId = ?, updatedAt = ?, deletedAt = ?, clientUuid = COALESCE(clientUuid, ?) " +
                        "WHERE taskId = ? AND userId = ?",
                item.getTitle(), item.getDescription(), item.getPriority(),
                item.getIsCompleted() != null && item.getIsCompleted(),
                item.getIsHabit() != null && item.getIsHabit(),
                item.getOrderIndex() != null ? item.getOrderIndex() : 0,
                item.getStartDate(), item.getDueDate(),
                item.getLifeAreaId(), item.getProjectId(),
                Timestamp.valueOf(updatedAt), toTimestamp(item.getDeletedAt()),
                item.getClientUuid(),
                item.getTaskId(), userId);
        if (localKey != null) response.getIdMap().put("task:" + localKey, item.getTaskId());
        return item.getTaskId();
    }

    private int insertTask(Integer userId, SyncPayloadDTO.TaskItem item, LocalDateTime updatedAt) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Tasks (userId, title, descriptionTask, priority, isCompleted, isHabit, " +
                            "orderIndex, startDate, dueDate, lifeAreaId, projectId, updatedAt, deletedAt, clientUuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setString(2, item.getTitle());
            ps.setString(3, item.getDescription());
            ps.setString(4, item.getPriority());
            ps.setBoolean(5, item.getIsCompleted() != null && item.getIsCompleted());
            ps.setBoolean(6, item.getIsHabit() != null && item.getIsHabit());
            ps.setInt(7, item.getOrderIndex() != null ? item.getOrderIndex() : 0);
            ps.setString(8, item.getStartDate());
            ps.setString(9, item.getDueDate());
            if (item.getLifeAreaId() == null) ps.setObject(10, null);
            else ps.setInt(10, item.getLifeAreaId());
            if (item.getProjectId() == null) ps.setObject(11, null);
            else ps.setInt(11, item.getProjectId());
            ps.setTimestamp(12, Timestamp.valueOf(updatedAt));
            ps.setTimestamp(13, toTimestamp(item.getDeletedAt()));
            ps.setString(14, item.getClientUuid());
            return ps;
        }, keys);
        return generatedId(keys);
    }

    // ---- focus sessions -----------------------------------------------

    private void upsertFocusSession(Integer userId, SyncPayloadDTO.FocusSessionItem item,
                                    LocalDateTime now, SyncPushResponseDTO response) {
        LocalDateTime updatedAt = effectiveUpdatedAt(item.getUpdatedAt(), now);
        Integer localKey = item.getLocalId() != null ? item.getLocalId() : item.getSessionId();

        Integer byUuid = findByClientUuid("FocusSessions", "sessionId", userId, item.getClientUuid());
        if (byUuid != null) {
            if (localKey != null) response.getIdMap().put("focusSession:" + localKey, byUuid);
            return;
        }

        boolean create = item.getSessionId() == null || item.getSessionId() <= 0;
        int serverId;
        if (create) {
            serverId = insertFocusSession(userId, item, updatedAt);
        } else {
            Timestamp existing = jdbc.query(
                    "SELECT updatedAt FROM FocusSessions WHERE sessionId = ? AND userId = ?",
                    rs -> rs.next() ? rs.getTimestamp(1) : null,
                    item.getSessionId(), userId);
            if (existing == null) {
                serverId = insertFocusSession(userId, item, updatedAt);
            } else if (isServerNewer(existing, updatedAt)) {
                serverId = item.getSessionId();
            } else {
                jdbc.update(
                        "UPDATE FocusSessions SET taskId = ?, startedAt = ?, endedAt = ?, " +
                                "durationSeconds = ?, mode = ?, completed = ?, updatedAt = ?, deletedAt = ?, " +
                                "clientUuid = COALESCE(clientUuid, ?) WHERE sessionId = ? AND userId = ?",
                        item.getTaskId(),
                        item.getStartedAt(), item.getEndedAt(),
                        item.getDurationSeconds(), item.getMode(),
                        item.getCompleted() == null || item.getCompleted(),
                        Timestamp.valueOf(updatedAt), toTimestamp(item.getDeletedAt()),
                        item.getClientUuid(),
                        item.getSessionId(), userId);
                serverId = item.getSessionId();
            }
        }
        if (localKey != null) response.getIdMap().put("focusSession:" + localKey, serverId);
    }

    private int insertFocusSession(Integer userId, SyncPayloadDTO.FocusSessionItem item,
                                   LocalDateTime updatedAt) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO FocusSessions (userId, taskId, startedAt, endedAt, durationSeconds, " +
                            "mode, completed, updatedAt, deletedAt, clientUuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            if (item.getTaskId() == null) ps.setObject(2, null);
            else ps.setInt(2, item.getTaskId());
            ps.setString(3, item.getStartedAt());
            ps.setString(4, item.getEndedAt());
            ps.setInt(5, item.getDurationSeconds() != null ? item.getDurationSeconds() : 0);
            ps.setString(6, item.getMode() != null ? item.getMode() : "FOCUS");
            ps.setBoolean(7, item.getCompleted() == null || item.getCompleted());
            ps.setTimestamp(8, Timestamp.valueOf(updatedAt));
            ps.setTimestamp(9, toTimestamp(item.getDeletedAt()));
            ps.setString(10, item.getClientUuid());
            return ps;
        }, keys);
        return generatedId(keys);
    }

    // ---- exams --------------------------------------------------------

    private int upsertExam(Integer userId, SyncPayloadDTO.ExamItem item,
                           LocalDateTime now, SyncPushResponseDTO response) {
        LocalDateTime updatedAt = effectiveUpdatedAt(item.getUpdatedAt(), now);
        Integer localKey = item.getLocalId() != null ? item.getLocalId() : item.getExamId();

        Integer byUuid = findByClientUuid("Exams", "examId", userId, item.getClientUuid());
        if (byUuid != null) {
            if (localKey != null) response.getIdMap().put("exam:" + localKey, byUuid);
            return byUuid;
        }

        boolean create = item.getExamId() == null || item.getExamId() <= 0;
        if (create) {
            int id = insertExam(userId, item, updatedAt);
            if (localKey != null) response.getIdMap().put("exam:" + localKey, id);
            return id;
        }

        Timestamp existing = jdbc.query(
                "SELECT updatedAt FROM Exams WHERE examId = ? AND userId = ?",
                rs -> rs.next() ? rs.getTimestamp(1) : null,
                item.getExamId(), userId);

        if (existing == null) {
            int id = insertExam(userId, item, updatedAt);
            if (localKey != null) response.getIdMap().put("exam:" + localKey, id);
            return id;
        }

        if (isServerNewer(existing, updatedAt)) {
            if (localKey != null) response.getIdMap().put("exam:" + localKey, item.getExamId());
            return item.getExamId();
        }

        jdbc.update(
                "UPDATE Exams SET lifeAreaId = ?, nameExam = ?, examType = ?, examDate = ?, classroom = ?, " +
                        "updatedAt = ?, deletedAt = ?, clientUuid = COALESCE(clientUuid, ?) " +
                        "WHERE examId = ? AND userId = ?",
                item.getLifeAreaId(), item.getNameExam(), item.getExamType(),
                item.getExamDate(), item.getClassroom(),
                Timestamp.valueOf(updatedAt), toTimestamp(item.getDeletedAt()),
                item.getClientUuid(),
                item.getExamId(), userId);
        if (localKey != null) response.getIdMap().put("exam:" + localKey, item.getExamId());
        return item.getExamId();
    }

    private int insertExam(Integer userId, SyncPayloadDTO.ExamItem item, LocalDateTime updatedAt) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Exams (userId, lifeAreaId, nameExam, examType, examDate, classroom, " +
                            "updatedAt, deletedAt, clientUuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            if (item.getLifeAreaId() == null) ps.setObject(2, null);
            else ps.setInt(2, item.getLifeAreaId());
            ps.setString(3, item.getNameExam() != null ? item.getNameExam() : "Exam");
            ps.setString(4, item.getExamType());
            if (item.getExamDate() == null) ps.setObject(5, null);
            else ps.setObject(5, LocalDate.parse(item.getExamDate()));
            ps.setString(6, item.getClassroom());
            ps.setTimestamp(7, Timestamp.valueOf(updatedAt));
            ps.setTimestamp(8, toTimestamp(item.getDeletedAt()));
            ps.setString(9, item.getClientUuid());
            return ps;
        }, keys);
        return generatedId(keys);
    }

    // ---- grades -------------------------------------------------------

    private int upsertGrade(Integer userId, SyncPayloadDTO.GradeItem item,
                            LocalDateTime now, SyncPushResponseDTO response) {
        LocalDateTime updatedAt = effectiveUpdatedAt(item.getUpdatedAt(), now);
        Integer localKey = item.getLocalId() != null ? item.getLocalId() : item.getGradeId();

        Integer byUuid = findByClientUuid("Grades", "gradeId", userId, item.getClientUuid());
        if (byUuid != null) {
            if (localKey != null) response.getIdMap().put("grade:" + localKey, byUuid);
            return byUuid;
        }

        boolean create = item.getGradeId() == null || item.getGradeId() <= 0;
        if (create) {
            int id = insertGrade(userId, item, updatedAt);
            if (localKey != null) response.getIdMap().put("grade:" + localKey, id);
            return id;
        }

        Timestamp existing = jdbc.query(
                "SELECT updatedAt FROM Grades WHERE gradeId = ? AND userId = ?",
                rs -> rs.next() ? rs.getTimestamp(1) : null,
                item.getGradeId(), userId);

        if (existing == null) {
            int id = insertGrade(userId, item, updatedAt);
            if (localKey != null) response.getIdMap().put("grade:" + localKey, id);
            return id;
        }

        if (isServerNewer(existing, updatedAt)) {
            if (localKey != null) response.getIdMap().put("grade:" + localKey, item.getGradeId());
            return item.getGradeId();
        }

        jdbc.update(
                "UPDATE Grades SET lifeAreaId = ?, concept = ?, score = ?, weight = ?, gradeDate = ?, " +
                        "updatedAt = ?, deletedAt = ?, clientUuid = COALESCE(clientUuid, ?) " +
                        "WHERE gradeId = ? AND userId = ?",
                item.getLifeAreaId(), item.getConcept(), item.getScore(), item.getWeight(),
                item.getGradeDate(),
                Timestamp.valueOf(updatedAt), toTimestamp(item.getDeletedAt()),
                item.getClientUuid(),
                item.getGradeId(), userId);
        if (localKey != null) response.getIdMap().put("grade:" + localKey, item.getGradeId());
        return item.getGradeId();
    }

    private int insertGrade(Integer userId, SyncPayloadDTO.GradeItem item, LocalDateTime updatedAt) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Grades (userId, lifeAreaId, concept, score, weight, gradeDate, " +
                            "updatedAt, deletedAt, clientUuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            if (item.getLifeAreaId() == null) ps.setObject(2, null);
            else ps.setInt(2, item.getLifeAreaId());
            ps.setString(3, item.getConcept());
            if (item.getScore() == null) ps.setObject(4, null);
            else ps.setDouble(4, item.getScore());
            if (item.getWeight() == null) ps.setObject(5, null);
            else ps.setDouble(5, item.getWeight());
            ps.setString(6, item.getGradeDate());
            ps.setTimestamp(7, Timestamp.valueOf(updatedAt));
            ps.setTimestamp(8, toTimestamp(item.getDeletedAt()));
            ps.setString(9, item.getClientUuid());
            return ps;
        }, keys);
        return generatedId(keys);
    }

    // ---- reminders ----------------------------------------------------

    private int upsertReminder(Integer userId, SyncPayloadDTO.ReminderItem item,
                               LocalDateTime now, SyncPushResponseDTO response) {
        LocalDateTime updatedAt = effectiveUpdatedAt(item.getUpdatedAt(), now);
        Integer localKey = item.getLocalId() != null ? item.getLocalId() : item.getReminderId();

        Integer byUuid = findByClientUuid("Reminders", "reminderId", userId, item.getClientUuid());
        if (byUuid != null) {
            if (localKey != null) response.getIdMap().put("reminder:" + localKey, byUuid);
            return byUuid;
        }

        boolean create = item.getReminderId() == null || item.getReminderId() <= 0;
        if (create) {
            int id = insertReminder(userId, item, updatedAt);
            if (localKey != null) response.getIdMap().put("reminder:" + localKey, id);
            return id;
        }

        Timestamp existing = jdbc.query(
                "SELECT updatedAt FROM Reminders WHERE reminderId = ? AND userId = ?",
                rs -> rs.next() ? rs.getTimestamp(1) : null,
                item.getReminderId(), userId);

        if (existing == null) {
            int id = insertReminder(userId, item, updatedAt);
            if (localKey != null) response.getIdMap().put("reminder:" + localKey, id);
            return id;
        }

        if (isServerNewer(existing, updatedAt)) {
            if (localKey != null) response.getIdMap().put("reminder:" + localKey, item.getReminderId());
            return item.getReminderId();
        }

        jdbc.update(
                "UPDATE Reminders SET message = ?, date_Time = ?, taskId = ?, examId = ?, " +
                        "updatedAt = ?, deletedAt = ?, clientUuid = COALESCE(clientUuid, ?) " +
                        "WHERE reminderId = ? AND userId = ?",
                item.getMessage(), toTimestamp(parseLocal(item.getDateTime())),
                item.getTaskId(), item.getExamId(),
                Timestamp.valueOf(updatedAt), toTimestamp(item.getDeletedAt()),
                item.getClientUuid(),
                item.getReminderId(), userId);
        if (localKey != null) response.getIdMap().put("reminder:" + localKey, item.getReminderId());
        return item.getReminderId();
    }

    private int insertReminder(Integer userId, SyncPayloadDTO.ReminderItem item, LocalDateTime updatedAt) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Reminders (userId, message, date_Time, taskId, examId, " +
                            "updatedAt, deletedAt, clientUuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setString(2, item.getMessage());
            ps.setTimestamp(3, toTimestamp(parseLocal(item.getDateTime())));
            if (item.getTaskId() == null) ps.setObject(4, null);
            else ps.setInt(4, item.getTaskId());
            if (item.getExamId() == null) ps.setObject(5, null);
            else ps.setInt(5, item.getExamId());
            ps.setTimestamp(6, Timestamp.valueOf(updatedAt));
            ps.setTimestamp(7, toTimestamp(item.getDeletedAt()));
            ps.setString(8, item.getClientUuid());
            return ps;
        }, keys);
        return generatedId(keys);
    }

    // ---- settings -----------------------------------------------------

    private void upsertSetting(Integer userId, SyncPayloadDTO.SettingItem item, LocalDateTime now) {
        LocalDateTime updatedAt = effectiveUpdatedAt(item.getUpdatedAt(), now);
        Timestamp existing = jdbc.query(
                "SELECT updatedAt FROM UserSettings WHERE userId = ? AND settingKey = ?",
                rs -> rs.next() ? rs.getTimestamp(1) : null,
                userId, item.getSettingKey());

        if (existing == null) {
            jdbc.update(
                    "INSERT INTO UserSettings (userId, settingKey, settingValue, updatedAt) VALUES (?, ?, ?, ?)",
                    userId, item.getSettingKey(), item.getSettingValue(), Timestamp.valueOf(updatedAt));
            return;
        }
        if (isServerNewer(existing, updatedAt)) return;

        jdbc.update(
                "UPDATE UserSettings SET settingValue = ?, updatedAt = ? WHERE userId = ? AND settingKey = ?",
                item.getSettingValue(), Timestamp.valueOf(updatedAt), userId, item.getSettingKey());
    }

    // ---- helpers ------------------------------------------------------

    /** Idempotency: same clientUuid already stored for this user → return existing id. */
    private Integer findByClientUuid(String table, String pk, Integer userId, String clientUuid) {
        if (clientUuid == null || clientUuid.isBlank()) return null;
        return jdbc.query(
                "SELECT " + pk + " FROM " + table + " WHERE userId = ? AND clientUuid = ?",
                rs -> rs.next() ? rs.getInt(1) : null,
                userId, clientUuid);
    }

    private static boolean isServerNewer(Timestamp existing, LocalDateTime clientUpdated) {
        if (existing == null) return false;
        LocalDateTime serverAt = existing.toLocalDateTime();
        // Strict after: equal timestamps let the client win (idempotent re-push).
        return serverAt.isAfter(clientUpdated);
    }

    private static LocalDateTime effectiveUpdatedAt(LocalDateTime client, LocalDateTime now) {
        if (client == null) return now;
        return client.isAfter(now) ? now : client;
    }

    private boolean ownsLifeArea(Integer userId, Integer lifeAreaId) {
        Integer found = jdbc.query(
                "SELECT lifeAreaId FROM LifeAreas WHERE lifeAreaId = ? AND userId = ?",
                rs -> rs.next() ? rs.getInt(1) : null,
                lifeAreaId, userId);
        return found != null;
    }

    private boolean ownsTask(Integer userId, Integer taskId) {
        Integer found = jdbc.query(
                "SELECT taskId FROM Tasks WHERE taskId = ? AND userId = ?",
                rs -> rs.next() ? rs.getInt(1) : null,
                taskId, userId);
        return found != null;
    }

    private boolean ownsExam(Integer userId, Integer examId) {
        Integer found = jdbc.query(
                "SELECT examId FROM Exams WHERE examId = ? AND userId = ?",
                rs -> rs.next() ? rs.getInt(1) : null,
                examId, userId);
        return found != null;
    }

    private static LocalDateTime parseLocal(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception e) {
            try {
                return java.time.OffsetDateTime.parse(raw).toLocalDateTime();
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private static Integer getIntOrNull(java.sql.ResultSet rs, String col) throws java.sql.SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    private static Timestamp toTimestamp(LocalDateTime dt) {
        return dt == null ? null : Timestamp.valueOf(dt);
    }

    private static int generatedId(KeyHolder keys) {
        Number key = keys.getKey();
        if (key == null) {
            List<Map<String, Object>> rows = keys.getKeyList();
            if (rows.isEmpty()) throw new IllegalStateException("No generated key returned");
            Object v = rows.get(0).values().iterator().next();
            return ((Number) v).intValue();
        }
        return key.intValue();
    }
}
