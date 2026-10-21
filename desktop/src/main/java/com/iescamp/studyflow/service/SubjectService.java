package com.iescamp.studyflow.service;

import com.iescamp.studyflow.database.DatabaseHelper;
import com.iescamp.studyflow.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Local-first subject (LifeArea) store. Mutations mark PENDING_SYNC;
 * SyncService pushes them and remaps negative ids. No HTTP on the CRUD path.
 */
public class SubjectService {

    public List<Subject> getAllSubjects() throws Exception {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT lifeAreaId, name, color, active, academicYear, updatedAt, deletedAt, syncStatus " +
                "FROM LifeAreas WHERE deletedAt IS NULL ORDER BY lifeAreaId ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Subject s = new Subject();
                s.setSubjectId(rs.getInt("lifeAreaId"));
                s.setNameSubject(rs.getString("name"));
                s.setColor(rs.getString("color"));
                s.setActiveSubject(rs.getInt("active") != 0);
                s.setAcademicYear(rs.getString("academicYear"));
                subjects.add(s);
            }
        }
        return subjects;
    }

    public void saveSubject(Subject subject) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            int id = subject.getSubjectId() <= 0
                    ? DatabaseHelper.nextLocalId(conn, "LifeAreas", "lifeAreaId")
                    : subject.getSubjectId();
            subject.setSubjectId(id);

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO LifeAreas (lifeAreaId, name, color, active, academicYear, " +
                            "updatedAt, deletedAt, syncStatus, clientUuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, NULL, 'PENDING_SYNC', ?)")) {
                ps.setInt(1, id);
                ps.setString(2, subject.getNameSubject());
                ps.setString(3, subject.getColor());
                ps.setInt(4, subject.isActiveSubject() ? 1 : 0);
                ps.setString(5, subject.getAcademicYear());
                ps.setString(6, now.toString());
                ps.setString(7, UUID.randomUUID().toString());
                ps.executeUpdate();
            }
        }
    }

    public void updateSubject(Integer id, Subject subject) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE LifeAreas SET name = ?, color = ?, active = ?, academicYear = ?, " +
                            "updatedAt = ?, syncStatus = 'PENDING_SYNC' WHERE lifeAreaId = ?")) {
                ps.setString(1, subject.getNameSubject());
                ps.setString(2, subject.getColor());
                ps.setInt(3, subject.isActiveSubject() ? 1 : 0);
                ps.setString(4, subject.getAcademicYear());
                ps.setString(5, now.toString());
                ps.setInt(6, id);
                ps.executeUpdate();
            }
        }
    }

    /** Soft delete: tombstone for sync, hidden from UI reads. */
    public void deleteSubject(Integer id) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE LifeAreas SET deletedAt = ?, updatedAt = ?, syncStatus = 'PENDING_SYNC' " +
                            "WHERE lifeAreaId = ?")) {
                ps.setString(1, now.toString());
                ps.setString(2, now.toString());
                ps.setInt(3, id);
                ps.executeUpdate();
            }
        }
    }

}
