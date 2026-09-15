package com.iescamp.studyflow_api.dto;

import lombok.Data;

@Data
public class NyraIngestRequestDTO {
    private String text; // The raw voice transcript or text command
}
