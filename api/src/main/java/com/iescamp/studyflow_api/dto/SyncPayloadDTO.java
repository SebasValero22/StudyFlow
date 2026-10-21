package com.iescamp.studyflow_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Wire contract for /api/sync/pull and /api/sync/push.
 * Scalar FKs (lifeAreaId, taskId) so negative local ids can be remapped.
 * deletedAt non-null = tombstone (soft-deleted row that must propagate).
 */
@Data
public class SyncPayloadDTO {

    @Data
    @NoArgsConstructor
    public static class LifeAreaItem {
        /** Client-local id (may be negative). Echoed into the push id map. */
        private Integer localId;
        /** Server id; null/negative on first push. */
        private Integer lifeAreaId;
        private String name;
        private String color;
        private Boolean active;
        private String academicYear;
        private String clientUuid;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
    }

    @Data
    @NoArgsConstructor
    public static class TaskItem {
        private Integer localId;
        private Integer taskId;
        private Integer lifeAreaId;
        private Integer projectId;
        private String title;
        private String description;
        private String priority;
        private Boolean isCompleted;
        private Boolean isHabit;
        private Integer orderIndex;
        private String startDate;
        private String dueDate;
        private String clientUuid;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
    }

    @Data
    @NoArgsConstructor
    public static class FocusSessionItem {
        private Integer localId;
        private Integer sessionId;
        private Integer taskId;
        private String startedAt;
        private String endedAt;
        private Integer durationSeconds;
        private String mode;
        private Boolean completed;
        private String clientUuid;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
    }

    @Data
    @NoArgsConstructor
    public static class SettingItem {
        private String settingKey;
        private String settingValue;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    public static class ExamItem {
        private Integer localId;
        private Integer examId;
        private Integer lifeAreaId;
        private String nameExam;
        private String examType;
        private String examDate;
        private String classroom;
        private String clientUuid;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
    }

    @Data
    @NoArgsConstructor
    public static class GradeItem {
        private Integer localId;
        private Integer gradeId;
        private Integer lifeAreaId;
        private String concept;
        private Double score;
        private Double weight;
        private String gradeDate;
        private String clientUuid;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
    }

    @Data
    @NoArgsConstructor
    public static class ReminderItem {
        private Integer localId;
        private Integer reminderId;
        private String message;
        private String dateTime;
        private Integer taskId;
        private Integer examId;
        private String clientUuid;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
    }

    private List<LifeAreaItem> lifeAreas = new ArrayList<>();
    private List<TaskItem> tasks = new ArrayList<>();
    private List<FocusSessionItem> focusSessions = new ArrayList<>();
    private List<ExamItem> exams = new ArrayList<>();
    private List<GradeItem> grades = new ArrayList<>();
    private List<ReminderItem> reminders = new ArrayList<>();
    private List<SettingItem> settings = new ArrayList<>();

    /** Wire protocol version. Server echoes its version; clients log on mismatch. */
    private Integer protocolVersion;

    /** Cursor for the next pull. Echoed by pull responses. */
    @JsonProperty("serverTime")
    private LocalDateTime serverTime;
}
