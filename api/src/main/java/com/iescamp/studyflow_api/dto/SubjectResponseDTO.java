package com.iescamp.studyflow_api.dto;

import com.iescamp.studyflow_api.model.Subject;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubjectResponseDTO {

    private Integer subjectId;

    private Integer userId;

    private String nameSubject; // Recomiendo usar nameSubject para coincidir con la Entidad
    private String color;
    private Boolean activeSubject;
    private String academicYear;

    public static SubjectResponseDTO convertToDTO(Subject entity) {
        SubjectResponseDTO dto = new SubjectResponseDTO();
        dto.setSubjectId(entity.getSubjectId());

        dto.setUserId(entity.getUserId());

        dto.setNameSubject(entity.getNameSubject());
        dto.setColor(entity.getColor());
        dto.setActiveSubject(entity.getActiveSubject());
        dto.setAcademicYear(entity.getAcademicYear());
        return dto;
    }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getNameSubject() { return nameSubject; }
    public void setNameSubject(String nameSubject) { this.nameSubject = nameSubject; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Boolean getActiveSubject() { return activeSubject; }
    public void setActiveSubject(Boolean activeSubject) { this.activeSubject = activeSubject; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
}
