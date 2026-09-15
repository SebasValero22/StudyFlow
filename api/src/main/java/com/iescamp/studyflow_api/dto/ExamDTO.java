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
    private Integer lifeAreaId;
    private String lifeAreaName;
    private String lifeAreaColor;

    // En ExamDTO.java (API)
    public static ExamDTO convertToDTO(Exam exam) {
        ExamDTO dto = new ExamDTO();
        dto.setExamId(exam.getExamId());
        dto.setNameExam(exam.getName()); // Asegúrate de que el DTO tenga este campo 'nameExam'
        dto.setExamType(exam.getExamType());
        dto.setExamDate(exam.getExamDate());
        dto.setClassroom(exam.getClassroom());

        if (exam.getLifeArea() != null) {
            dto.setLifeAreaId(exam.getLifeArea().getLifeAreaId());
            dto.setLifeAreaName(exam.getLifeArea().getName());
            dto.setLifeAreaColor(exam.getLifeArea().getColor());
        }
        return dto;
    }
}