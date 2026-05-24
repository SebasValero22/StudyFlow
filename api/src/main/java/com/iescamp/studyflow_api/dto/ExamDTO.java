package com.iescamp.studyflow_api.dto;

import com.iescamp.studyflow_api.model.Exam;
import com.iescamp.studyflow_api.model.enums.ExamType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ExamDTO {
    private Integer examId;
    private String nameExam;  // Cambiado de 'name' a 'nameExam' para el Desktop
    private String examType;
    private LocalDate examDate; // Cambiado java.sql.Date por LocalDate para evitar problemas de Epoch
    private String classroom;
    private Integer subjectId;
    private String subjectName;
    private String subjectColor;

    public static ExamDTO convertToDTO(Exam exam) {
        ExamDTO dto = new ExamDTO();
        dto.setExamId(exam.getExamId());
        dto.setNameExam(exam.getName());
        dto.setExamType(exam.getExamType());
        dto.setExamDate(exam.getExamDate());
        dto.setClassroom(exam.getClassroom());

        if (exam.getSubject() != null) {
            dto.setSubjectId(exam.getSubject().getSubjectId());
            dto.setSubjectName(exam.getSubject().getNameSubject());
            dto.setSubjectColor(exam.getSubject().getColor());
        }
        return dto;
    }
}
