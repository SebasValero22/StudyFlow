package com.iescamp.studyflow_api.dto;

import java.util.List;

public record FocusReportDTO(
        long totalFocusSeconds,
        long sessionCount,
        List<DayTotal> byDay
) {
    public record DayTotal(String date, long seconds) {}
}
