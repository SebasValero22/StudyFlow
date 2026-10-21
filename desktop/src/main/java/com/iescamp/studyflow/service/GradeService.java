package com.iescamp.studyflow.service;

import com.iescamp.studyflow.database.DatabaseHelper;
import com.iescamp.studyflow.model.Grade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Local-first grade store (SQLite). Mutations mark PENDING_SYNC; SyncService pushes them.
 */
public class GradeService {

    public List<Grade> getAllGrades() throws Exception {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT gradeId, subjectId, concept, score, weight, gradeDate, " +
                "updatedAt, deletedAt, syncStatus, clientUuid " +
                "FROM Grades WHERE deletedAt IS NULL ORDER BY gradeDate DESC, gradeId ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                grades.add(mapGrade(rs));
            }
        }
        return grades;
    }

    public void saveGrade(Grade grade) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            Integer id = grade.getGradeId();
            if (id == null || id <= 0) {
                id = DatabaseHelper.nextLocalId(conn, "Grades", "gradeId");
                grade.setGradeId(id);
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Grades (gradeId, subjectId, concept, score, weight, gradeDate, " +
                            "updatedAt, deletedAt, syncStatus, clientUuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, NULL, 'PENDING_SYNC', ?)")) {
                ps.setInt(1, id);
                if (grade.getSubjectId() == null) ps.setObject(2, null);
                else ps.setInt(2, grade.getSubjectId());
                ps.setString(3, grade.getConcept());
                ps.setDouble(4, grade.getScore() != null ? grade.getScore() : 0);
                ps.setDouble(5, grade.getWeight() != null ? grade.getWeight() : 0);
                ps.setString(6, grade.getGradeDate());
                ps.setString(7, now.toString());
                ps.setString(8, grade.getClientUuid() != null ? grade.getClientUuid() : UUID.randomUUID().toString());
                ps.executeUpdate();
            }
        }
    }

    public void updateGrade(Integer id, Grade grade) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Grades SET subjectId = ?, concept = ?, score = ?, weight = ?, gradeDate = ?, " +
                            "updatedAt = ?, syncStatus = 'PENDING_SYNC' WHERE gradeId = ?")) {
                if (grade.getSubjectId() == null) ps.setObject(1, null);
                else ps.setInt(1, grade.getSubjectId());
                ps.setString(2, grade.getConcept());
                ps.setDouble(3, grade.getScore() != null ? grade.getScore() : 0);
                ps.setDouble(4, grade.getWeight() != null ? grade.getWeight() : 0);
                ps.setString(5, grade.getGradeDate());
                ps.setString(6, now.toString());
                ps.setInt(7, id);
                ps.executeUpdate();
            }
        }
    }

    public void deleteGrade(Integer id) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Grades SET deletedAt = ?, updatedAt = ?, syncStatus = 'PENDING_SYNC' WHERE gradeId = ?")) {
                ps.setString(1, now.toString());
                ps.setString(2, now.toString());
                ps.setInt(3, id);
                ps.executeUpdate();
            }
        }
    }

    private static Grade mapGrade(ResultSet rs) throws SQLException {
        Grade g = new Grade();
        g.setGradeId(rs.getInt("gradeId"));
        int sid = rs.getInt("subjectId");
        if (!rs.wasNull()) g.setSubjectId(sid);
        g.setConcept(rs.getString("concept"));
        g.setScore(rs.getDouble("score"));
        g.setWeight(rs.getDouble("weight"));
        g.setGradeDate(rs.getString("gradeDate"));
        String updatedAt = rs.getString("updatedAt");
        if (updatedAt != null) g.setUpdatedAt(LocalDateTime.parse(updatedAt));
        String deletedAt = rs.getString("deletedAt");
        if (deletedAt != null) g.setDeletedAt(LocalDateTime.parse(deletedAt));
        g.setSyncStatus(rs.getString("syncStatus"));
        g.setClientUuid(rs.getString("clientUuid"));
        return g;
    }
}
