package com.iescamp.studyflow_api.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class NyraActionResponseDTO {
    private String action; // CREATE, UPDATE, DELETE, AUTO_SCHEDULE, NONE
    private String intentTitle; 
    private String description;
    
    // Context mapping
    private String suggestedLifeArea; 
    private String suggestedProject; 
    
    // Temporal mapping
    private LocalDate suggestedStartDate;
    private LocalDate suggestedDueDate;
    
    // Metadata
    private String priority; // HIGH, MEDIUM, LOW
    private Boolean isHabit;
}
