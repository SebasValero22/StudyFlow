package com.iescamp.studyflow_api.dto;
import com.iescamp.studyflow_api.model.Subject;
import lombok.Data;

@Data
public class SubjectResponseDTO {
    private Integer subjectId;
    private String subjectName;
    private String color;
    private boolean active;
    private String academicYear;

    public static SubjectResponseDTO convertToDTO(Subject entity) {
        SubjectResponseDTO dto = new SubjectResponseDTO();
        dto.setSubjectId(entity.getSubjectId());
        dto.setSubjectName(entity.getName());
        dto.setColor(entity.getColor());
        dto.setActive(entity.isActive());
        dto.setAcademicYear(entity.getAcademicYear());
        return dto;
    }
}