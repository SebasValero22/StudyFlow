package com.iescamp.studyflow_api.dto;

import lombok.Data;

@Data
public class NyraAutoScheduleRequestDTO {
    private String preferences; // E.g. "I prefer mornings, no weekends"
}
