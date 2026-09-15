package com.iescamp.studyflow_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iescamp.studyflow_api.model.LifeArea;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LifeAreaResponseDTO {

    // Explicit wire name: Boot 4's strict bean naming would otherwise emit "LifeAreaId".
    @JsonProperty("lifeAreaId")
    private Integer lifeAreaId;

    private Integer userId;

    private String name;
    private String color;
    private Boolean active;
    private String academicYear;

    public static LifeAreaResponseDTO convertToDTO(LifeArea entity) {
        LifeAreaResponseDTO dto = new LifeAreaResponseDTO();
        dto.setLifeAreaId(entity.getLifeAreaId());
        dto.setUserId(entity.getUserId());
        dto.setName(entity.getName());
        dto.setColor(entity.getColor());
        dto.setActive(entity.getActive());
        dto.setAcademicYear(entity.getAcademicYear());
        return dto;
    }
}
