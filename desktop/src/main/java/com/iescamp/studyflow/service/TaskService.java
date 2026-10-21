package com.iescamp.studyflow.service;

import com.iescamp.studyflow.database.DatabaseHelper;
import com.iescamp.studyflow.model.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Local-first task store (SQLite). Mutations mark PENDING_SYNC; the sync engine
 * pushes them and remaps negative ids. No HTTP on the CRUD path.
 */
public class TaskService {

    public List<Task> getAllTasks() throws Exception {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.taskId, t.title, t.description, t.isCompleted, t.isHabit, t.priority, " +
                "t.startDate, t.dueDate, t.lifeAreaId, t.projectId, t.orderIndex, " +
                "t.updatedAt, t.deletedAt, t.syncStatus, " +
                "t.trigger, t.microStep, t.energyBucket, " +
                "l.name AS subjectName, l.color AS subjectColor " +
                "FROM Tasks t LEFT JOIN LifeAreas l ON l.lifeAreaId = t.lifeAreaId " +
                "WHERE t.deletedAt IS NULL " +
                "ORDER BY t.orderIndex ASC, t.taskId ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tasks.add(mapTask(rs));
            }
        }
        return tasks;
    }

    public void saveTask(Task task) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try {
                LocalDateTime now = LocalDateTime.now();
                int id = task.getTaskId() == null || task.getTaskId() <= 0
                        ? DatabaseHelper.nextLocalId(conn, "Tasks", "taskId")
                        : task.getTaskId();
                task.setTaskId(id);

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Tasks (taskId, title, description, isCompleted, isHabit, priority, " +
                                "startDate, dueDate, lifeAreaId, projectId, orderIndex, updatedAt, deletedAt, syncStatus, clientUuid, " +
                                "trigger, microStep, energyBucket) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, 'PENDING_SYNC', ?, ?, ?, ?)")) {
                    bindTaskForWrite(ps, id, task, now);
                    ps.setString(13, task.getClientUuid() != null
                            ? task.getClientUuid()
                            : java.util.UUID.randomUUID().toString());
                    ps.setString(14, task.getTrigger());
                    ps.setString(15, task.getMicroStep());
                    ps.setString(16, task.getEnergyBucket());
                    ps.executeUpdate();
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

    public void updateTask(Integer id, Task task) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Tasks SET title = ?, description = ?, isCompleted = ?, isHabit = ?, " +
                            "priority = ?, startDate = ?, dueDate = ?, lifeAreaId = ?, projectId = ?, " +
                            "orderIndex = ?, updatedAt = ?, syncStatus = 'PENDING_SYNC', " +
                            "trigger = ?, microStep = ?, energyBucket = ? " +
                            "WHERE taskId = ?")) {
                ps.setString(1, task.getTitle());
                ps.setString(2, task.getDescriptionTask());
                ps.setInt(3, task.getIsCompleted() != null && task.getIsCompleted() ? 1 : 0);
                ps.setInt(4, task.getIsHabit() != null && task.getIsHabit() ? 1 : 0);
                ps.setString(5, task.getPriority());
                ps.setString(6, task.getStartDate() != null ? task.getStartDate().toString() : null);
                ps.setString(7, task.getDueDate() != null ? task.getDueDate().toString() : null);
                if (task.getSubjectId() == null) ps.setObject(8, null);
                else ps.setInt(8, task.getSubjectId());
                if (task.getProjectId() == null) ps.setObject(9, null);
                else ps.setInt(9, task.getProjectId());
                ps.setInt(10, task.getOrderIndex() != null ? task.getOrderIndex() : 0);
                ps.setString(11, now.toString());
                ps.setString(12, task.getTrigger());
                ps.setString(13, task.getMicroStep());
                ps.setString(14, task.getEnergyBucket());
                ps.setInt(15, id);
                ps.executeUpdate();
            }
        }
    }

    /** Soft delete: tombstone for sync, hidden from UI reads. */
    public void deleteTask(Integer id) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Tasks SET deletedAt = ?, updatedAt = ?, syncStatus = 'PENDING_SYNC' WHERE taskId = ?")) {
                ps.setString(1, now.toString());
                ps.setString(2, now.toString());
                ps.setInt(3, id);
                ps.executeUpdate();
            }
        }
    }

    private static void bindTaskForWrite(PreparedStatement ps, int id, Task task, LocalDateTime now)
            throws SQLException {
        ps.setInt(1, id);
        ps.setString(2, task.getTitle());
        ps.setString(3, task.getDescriptionTask());
        ps.setInt(4, task.getIsCompleted() != null && task.getIsCompleted() ? 1 : 0);
        ps.setInt(5, task.getIsHabit() != null && task.getIsHabit() ? 1 : 0);
        ps.setString(6, task.getPriority());
        ps.setString(7, task.getStartDate() != null ? task.getStartDate().toString() : null);
        ps.setString(8, task.getDueDate() != null ? task.getDueDate().toString() : null);
        if (task.getSubjectId() == null) ps.setObject(9, null);
        else ps.setInt(9, task.getSubjectId());
        if (task.getProjectId() == null) ps.setObject(10, null);
        else ps.setInt(10, task.getProjectId());
        ps.setInt(11, task.getOrderIndex() != null ? task.getOrderIndex() : 0);
        ps.setString(12, now.toString());
        ps.setString(13, task.getTrigger());
        ps.setString(14, task.getMicroStep());
        ps.setString(15, task.getEnergyBucket());
    }

    private static Task mapTask(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setTaskId(rs.getInt("taskId"));
        t.setTitle(rs.getString("title"));
        t.setDescriptionTask(rs.getString("description"));
        t.setIsCompleted(rs.getInt("isCompleted") == 1);
        t.setIsHabit(rs.getInt("isHabit") == 1);
        t.setPriority(rs.getString("priority"));
        String start = rs.getString("startDate");
        if (start != null) t.setStartDate(LocalDate.parse(start));
        String due = rs.getString("dueDate");
        if (due != null) t.setDueDate(LocalDate.parse(due));
        int lifeAreaId = rs.getInt("lifeAreaId");
        if (!rs.wasNull()) t.setSubjectId(lifeAreaId);
        int projectId = rs.getInt("projectId");
        if (!rs.wasNull()) t.setProjectId(projectId);
        t.setOrderIndex(rs.getInt("orderIndex"));
        t.setSubjectName(rs.getString("subjectName"));
        t.setSubjectColor(rs.getString("subjectColor"));
        String updatedAt = rs.getString("updatedAt");
        if (updatedAt != null) t.setUpdatedAt(LocalDateTime.parse(updatedAt));
        String deletedAt = rs.getString("deletedAt");
        if (deletedAt != null) t.setDeletedAt(LocalDateTime.parse(deletedAt));
        t.setSyncStatus(rs.getString("syncStatus"));
        t.setTrigger(rs.getString("trigger"));
        t.setMicroStep(rs.getString("microStep"));
        t.setEnergyBucket(rs.getString("energyBucket"));
        return t;
    }
}
