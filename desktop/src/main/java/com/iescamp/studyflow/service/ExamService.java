package com.iescamp.studyflow.service;

import com.iescamp.studyflow.database.DatabaseHelper;
import com.iescamp.studyflow.model.Exam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Local-first exam store (SQLite). Mutations mark PENDING_SYNC; SyncService pushes them.
 */
public class ExamService {

    public List<Exam> getAllExams() throws Exception {
        List<Exam> exams = new ArrayList<>();
        String sql = "SELECT e.examId, e.subjectId, e.nameExam, e.examType, e.examDate, e.classroom, " +
                "e.updatedAt, e.deletedAt, e.syncStatus, e.clientUuid, " +
                "l.name AS subjectName, l.color AS subjectColor " +
                "FROM Exams e LEFT JOIN LifeAreas l ON l.lifeAreaId = e.subjectId " +
                "WHERE e.deletedAt IS NULL ORDER BY e.examDate ASC, e.examId ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                exams.add(mapExam(rs));
            }
        }
        return exams;
    }

    public List<Exam> getExamsBySubject(Integer subjectId) throws Exception {
        List<Exam> all = getAllExams();
        List<Exam> out = new ArrayList<>();
        for (Exam e : all) {
            if (subjectId != null && subjectId.equals(e.getSubjectId())) {
                out.add(e);
            }
        }
        return out;
    }

    public void saveExam(Exam exam) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            Integer id = exam.getExamId();
            if (id == null || id <= 0) {
                id = DatabaseHelper.nextLocalId(conn, "Exams", "examId");
                exam.setExamId(id);
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Exams (examId, subjectId, nameExam, examType, examDate, classroom, " +
                            "updatedAt, deletedAt, syncStatus, clientUuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, NULL, 'PENDING_SYNC', ?)")) {
                ps.setInt(1, id);
                if (exam.getSubjectId() == null) ps.setObject(2, null);
                else ps.setInt(2, exam.getSubjectId());
                ps.setString(3, exam.getNameExam());
                ps.setString(4, exam.getExamType());
                ps.setString(5, exam.getExamDate() != null ? exam.getExamDate().toString() : null);
                ps.setString(6, exam.getClassroom());
                ps.setString(7, now.toString());
                ps.setString(8, exam.getClientUuid() != null ? exam.getClientUuid() : UUID.randomUUID().toString());
                ps.executeUpdate();
            }
        }
    }

    public void updateExam(Integer id, Exam exam) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Exams SET subjectId = ?, nameExam = ?, examType = ?, examDate = ?, " +
                            "classroom = ?, updatedAt = ?, syncStatus = 'PENDING_SYNC' WHERE examId = ?")) {
                if (exam.getSubjectId() == null) ps.setObject(1, null);
                else ps.setInt(1, exam.getSubjectId());
                ps.setString(2, exam.getNameExam());
                ps.setString(3, exam.getExamType());
                ps.setString(4, exam.getExamDate() != null ? exam.getExamDate().toString() : null);
                ps.setString(5, exam.getClassroom());
                ps.setString(6, now.toString());
                ps.setInt(7, id);
                ps.executeUpdate();
            }
        }
    }

    public void deleteExam(Integer id) throws Exception {
        try (Connection conn = DatabaseHelper.getConnection()) {
            LocalDateTime now = LocalDateTime.now();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Exams SET deletedAt = ?, updatedAt = ?, syncStatus = 'PENDING_SYNC' WHERE examId = ?")) {
                ps.setString(1, now.toString());
                ps.setString(2, now.toString());
                ps.setInt(3, id);
                ps.executeUpdate();
            }
        }
    }

    private static Exam mapExam(ResultSet rs) throws SQLException {
        Exam e = new Exam();
        e.setExamId(rs.getInt("examId"));
        int sid = rs.getInt("subjectId");
        if (!rs.wasNull()) e.setSubjectId(sid);
        e.setNameExam(rs.getString("nameExam"));
        e.setExamType(rs.getString("examType"));
        String date = rs.getString("examDate");
        if (date != null) e.setExamDate(LocalDate.parse(date));
        e.setClassroom(rs.getString("classroom"));
        e.setSubjectName(rs.getString("subjectName"));
        e.setSubjectColor(rs.getString("subjectColor"));
        String updatedAt = rs.getString("updatedAt");
        if (updatedAt != null) e.setUpdatedAt(LocalDateTime.parse(updatedAt));
        String deletedAt = rs.getString("deletedAt");
        if (deletedAt != null) e.setDeletedAt(LocalDateTime.parse(deletedAt));
        e.setSyncStatus(rs.getString("syncStatus"));
        e.setClientUuid(rs.getString("clientUuid"));
        return e;
    }
}
