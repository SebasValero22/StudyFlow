package com.iescamp.studyflow.service;

import com.iescamp.studyflow.database.DatabaseHelper;
import com.iescamp.studyflow.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    private TaskService taskService;

    @BeforeAll
    static void initVault() throws Exception {
        Path temp = Files.createTempDirectory("studyflow-task-test");
        System.setProperty("studyflow.vault", temp.toString());
        DatabaseHelper.initializeSchema();
    }

    @BeforeEach
    void setUp() {
        taskService = new TaskService();
    }

    @AfterEach
    void cleanTable() throws Exception {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM Tasks")) {
            ps.executeUpdate();
        }
    }

    @Test
    void saveTaskCreatesNegativeIdAndPendingSync() throws Exception {
        Task t = new Task();
        t.setTitle("Write tests");
        t.setPriority("HIGH");
        t.setDueDate(LocalDate.of(2026, 1, 15));
        t.setClientUuid(UUID.randomUUID().toString());

        taskService.saveTask(t);

        assertNotNull(t.getTaskId());
        assertTrue(t.getTaskId() < 0, "offline create must use negative PK");

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT syncStatus, clientUuid FROM Tasks WHERE taskId = ?")) {
            ps.setInt(1, t.getTaskId());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("PENDING_SYNC", rs.getString("syncStatus"));
                assertNotNull(rs.getString("clientUuid"));
            }
        }
    }

    @Test
    void getAllTasksFiltersTombstones() throws Exception {
        Task keep = new Task();
        keep.setTitle("Visible");
        taskService.saveTask(keep);

        Task doomed = new Task();
        doomed.setTitle("Deleted");
        taskService.saveTask(doomed);
        taskService.deleteTask(doomed.getTaskId());

        List<Task> tasks = taskService.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Visible", tasks.get(0).getTitle());
    }

    @Test
    void updateTaskMarksPendingAndBumpsUpdatedAt() throws Exception {
        Task t = new Task();
        t.setTitle("Before");
        taskService.saveTask(t);

        Task patch = new Task();
        patch.setTitle("After");
        patch.setPriority("LOW");
        taskService.updateTask(t.getTaskId(), patch);

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT title, syncStatus, updatedAt FROM Tasks WHERE taskId = ?")) {
            ps.setInt(1, t.getTaskId());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("After", rs.getString("title"));
                assertEquals("PENDING_SYNC", rs.getString("syncStatus"));
                assertNotNull(rs.getString("updatedAt"));
            }
        }
    }

    @Test
    void softDeleteSetsTombstoneNotHardDelete() throws Exception {
        Task t = new Task();
        t.setTitle("To delete");
        taskService.saveTask(t);
        taskService.deleteTask(t.getTaskId());

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT deletedAt FROM Tasks WHERE taskId = ?")) {
            ps.setInt(1, t.getTaskId());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "row must remain for tombstone sync");
                assertNotNull(rs.getString("deletedAt"));
            }
        }
        assertTrue(taskService.getAllTasks().isEmpty());
    }
}
