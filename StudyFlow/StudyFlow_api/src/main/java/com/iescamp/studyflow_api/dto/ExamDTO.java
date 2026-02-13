package com.iescamp.studyflow_api.dto;

import com.iescamp.studyflow_api.model.Exam;
import com.iescamp.studyflow_api.model.Subject;
import com.iescamp.studyflow_api.model.enums.ExamType;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class ExamDTO {

    private Integer examId;
    private String name;
    private ExamType examType;
    private java.sql.Date examDate;
    private String classroom;
    private Subject subject;

    public static ExamDTO convertToDTO(Exam exam){
        ExamDTO dto = new ExamDTO();
        dto.setExamId(exam.getExamId());
        dto.setName(exam.getName());
        dto.setExamDate(exam.getExamDate());
        dto.setExamType(exam.getExamType());
        dto.setExamDate(exam.getExamDate());
        dto.setSubject(exam.getSubject());

        return dto;
    }

}
