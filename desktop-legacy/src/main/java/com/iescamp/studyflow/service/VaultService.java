package com.iescamp.studyflow.service;

import com.iescamp.studyflow.database.DatabaseHelper;
import com.iescamp.studyflow.model.VaultItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Local-first Vault store (SQLite). "Say No" Anti-FOMO Vault.
 * Items are quarantined ideas to protect Main Quest focus for 84 days.
 */
public class VaultService {

    public List<VaultItem> getAllVaultItems() throws Exception {
        List<VaultItem> items = new ArrayList<>();
        String sql = "SELECT vaultItemId, userId, title, domain, reason, updatedAt, deletedAt, syncStatus, clientUuid " +
                "FROM VaultItems WHERE deletedAt IS NULL ORDER BY updatedAt DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapVaultItem(rs));
            }
        }
        return items;
    }

    public void saveVaultItem(VaultItem item) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try {
                LocalDateTime now = LocalDateTime.now();
                int id = item.getVaultItemId() == null || item.getVaultItemId() <= 0
                        ? DatabaseHelper.nextLocalId(conn, "VaultItems", "vaultItemId")
                        : item.getVaultItemId();
                item.setVaultItemId(id);

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO VaultItems (vaultItemId, userId, title, domain, reason, updatedAt, deletedAt, syncStatus, clientUuid) " +
                                "VALUES (?, ?, ?, ?, ?, ?, NULL, 'PENDING_SYNC', ?)")) {
                    ps.setInt(1, id);
                    ps.setInt(2, item.getUserId() != null ? item.getUserId() : 1);
                    ps.setString(3, item.getTitle());
                    ps.setString(4, item.getDomain());
                    ps.setString(5, item.getReason());
                    ps.setString(6, now.toString());
                    ps.setString(7, item.getClientUuid() != null
                            ? item.getClientUuid()
                            : java.util.UUID.randomUUID().toString());
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

    public void updateVaultItem(Integer id, VaultItem item) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE VaultItems SET title = ?, domain = ?, reason = ?, updatedAt = ?, syncStatus = 'PENDING_SYNC' " +
                            "WHERE vaultItemId = ?")) {
                ps.setString(1, item.getTitle());
                ps.setString(2, item.getDomain());
                ps.setString(3, item.getReason());
                ps.setString(4, now.toString());
                ps.setInt(5, id);
                ps.executeUpdate();
            }
        }
    }

    /** Soft delete: tombstone for sync, hidden from UI reads. */
    public void deleteVaultItem(Integer id) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE VaultItems SET deletedAt = ?, updatedAt = ?, syncStatus = 'PENDING_SYNC' WHERE vaultItemId = ?")) {
                ps.setString(1, now.toString());
                ps.setString(2, now.toString());
                ps.setInt(3, id);
                ps.executeUpdate();
            }
        }
    }

    private static VaultItem mapVaultItem(ResultSet rs) throws SQLException {
        VaultItem v = new VaultItem();
        v.setVaultItemId(rs.getInt("vaultItemId"));
        v.setUserId(rs.getInt("userId"));
        v.setTitle(rs.getString("title"));
        v.setDomain(rs.getString("domain"));
        v.setReason(rs.getString("reason"));
        String updatedAt = rs.getString("updatedAt");
        if (updatedAt != null) v.setUpdatedAt(LocalDateTime.parse(updatedAt));
        String deletedAt = rs.getString("deletedAt");
        if (deletedAt != null) v.setDeletedAt(LocalDateTime.parse(deletedAt));
        v.setSyncStatus(rs.getString("syncStatus"));
        v.setClientUuid(rs.getString("clientUuid"));
        return v;
    }
}