package com.iescamp.studyflow.service;

import com.iescamp.studyflow.database.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Records when a PENDING_SYNC local edit is overwritten by a newer remote row
 * during pull (LWW loss), so silent data loss becomes visible to the user.
 * Conflicts are device-local, capped, and dismissible — never synced.
 */
public final class SyncConflictService {

    private static final int MAX_CONFLICTS = 50;

    /** One overwritten local edit, kept for display until dismissed. */
    public record Conflict(int conflictId, String entityType, int entityId,
                           String localSummary, String remoteSummary,
                           String localUpdatedAt, String remoteUpdatedAt, String detectedAt) {}

    private SyncConflictService() {}

    /** Called by SyncService inside the merge transaction. Replaces any undismissed conflict for the same row. */
    static void record(Connection conn, String entityType, int entityId,
                       String localSummary, String remoteSummary,
                       String localUpdatedAt, String remoteUpdatedAt) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM SyncConflicts WHERE entityType = ? AND entityId = ? AND dismissed = 0")) {
            ps.setString(1, entityType);
            ps.setInt(2, entityId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO SyncConflicts (entityType, entityId, localSummary, remoteSummary, " +
                        "localUpdatedAt, remoteUpdatedAt, detectedAt) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, entityType);
            ps.setInt(2, entityId);
            ps.setString(3, localSummary);
            ps.setString(4, remoteSummary);
            ps.setString(5, localUpdatedAt);
            ps.setString(6, remoteUpdatedAt);
            ps.setString(7, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
        prune(conn);
    }

    /** Newest-first list of undismissed conflicts. */
    public static List<Conflict> list() {
        List<Conflict> out = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT conflictId, entityType, entityId, localSummary, remoteSummary, " +
                             "localUpdatedAt, remoteUpdatedAt, detectedAt FROM SyncConflicts " +
                             "WHERE dismissed = 0 ORDER BY detectedAt DESC, conflictId DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Conflict(rs.getInt("conflictId"), rs.getString("entityType"),
                        rs.getInt("entityId"), rs.getString("localSummary"), rs.getString("remoteSummary"),
                        rs.getString("localUpdatedAt"), rs.getString("remoteUpdatedAt"),
                        rs.getString("detectedAt")));
            }
        } catch (Exception e) {
            System.err.println("Could not list sync conflicts: " + e.getMessage());
        }
        return out;
    }

    public static int activeCount() {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM SyncConflicts WHERE dismissed = 0");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            System.err.println("Could not count sync conflicts: " + e.getMessage());
            return 0;
        }
    }

    public static void dismissAll() {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE SyncConflicts SET dismissed = 1 WHERE dismissed = 0")) {
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Could not dismiss sync conflicts: " + e.getMessage());
        }
    }

    private static void prune(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM SyncConflicts WHERE conflictId NOT IN (" +
                    "SELECT conflictId FROM SyncConflicts ORDER BY detectedAt DESC, conflictId DESC LIMIT "
                    + MAX_CONFLICTS + ")");
        }
    }
}
