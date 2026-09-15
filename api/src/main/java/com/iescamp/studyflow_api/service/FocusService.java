package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.dto.FocusReportDTO;
import com.iescamp.studyflow_api.model.FocusSession;
import com.iescamp.studyflow_api.model.Task;
import com.iescamp.studyflow_api.model.enums.SessionMode;
import com.iescamp.studyflow_api.repository.FocusSessionRepository;
import com.iescamp.studyflow_api.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FocusService {

    private static final int MAX_SESSION_SECONDS = 8 * 60 * 60; // sanity cap: one workday

    private final FocusSessionRepository sessionRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public FocusService(FocusSessionRepository sessionRepository, TaskRepository taskRepository) {
        this.sessionRepository = sessionRepository;
        this.taskRepository = taskRepository;
    }

    public FocusSession record(Integer userId, Integer taskId, LocalDateTime startedAt,
                               Integer durationSeconds, String mode, Boolean completed) {
        if (durationSeconds == null || durationSeconds <= 0 || durationSeconds > MAX_SESSION_SECONDS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "durationSeconds must be between 1 and " + MAX_SESSION_SECONDS);
        }

        SessionMode sessionMode = parseMode(mode);
        if (taskId != null) {
            // The pinned task must belong to the same user (Animedoro lock integrity).
            taskRepository.findById(taskId)
                    .filter(t -> t.getUserId().equals(userId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task not found"));
        }

        FocusSession session = new FocusSession();
        session.setUserId(userId);
        session.setTaskId(taskId);
        session.setDurationSeconds(durationSeconds);
        session.setMode(sessionMode);
        session.setCompleted(completed == null || completed);
        LocalDateTime end = LocalDateTime.now();
        session.setEndedAt(end);
        session.setStartedAt(startedAt != null ? startedAt : end.minusSeconds(durationSeconds));

        return sessionRepository.save(session);
    }

    public List<FocusSession> history(Integer userId, LocalDate from) {
        LocalDateTime fromDt = (from != null ? from : LocalDate.now().minusDays(30)).atStartOfDay();
        return sessionRepository.findByUserIdAndStartedAtGreaterThanEqualOrderByStartedAtDesc(userId, fromDt);
    }

    public FocusReportDTO report(Integer userId, LocalDate from, LocalDate to) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusDays(6);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        if (effectiveTo.isBefore(effectiveFrom)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'to' is before 'from'");
        }
        LocalDateTime fromDt = effectiveFrom.atStartOfDay();
        LocalDateTime toDt = effectiveTo.plusDays(1).atStartOfDay(); // exclusive

        long total = sessionRepository.sumFocusSeconds(userId, fromDt, toDt);
        long count = sessionRepository.countFocusSessions(userId, fromDt, toDt);

        Map<LocalDate, Long> perDay = new LinkedHashMap<>();
        for (LocalDate d = effectiveFrom; !d.isAfter(effectiveTo); d = d.plusDays(1)) {
            perDay.put(d, 0L); // dense series -> Chart.js-ready
        }
        for (FocusSession s : sessionRepository
                .findByUserIdAndStartedAtGreaterThanEqualOrderByStartedAtDesc(userId, fromDt)) {
            LocalDate day = s.getStartedAt().toLocalDate();
            if (s.getMode() == SessionMode.FOCUS && perDay.containsKey(day)) {
                perDay.merge(day, (long) s.getDurationSeconds(), Long::sum);
            }
        }

        List<FocusReportDTO.DayTotal> byDay = new ArrayList<>();
        perDay.forEach((d, secs) -> byDay.add(new FocusReportDTO.DayTotal(d.toString(), secs)));

        return new FocusReportDTO(total, count, byDay);
    }

    /** RFC 4180-ish CSV timesheet: the export consultants ask for on day one. */
    public String timesheetCsv(Integer userId, LocalDate from, LocalDate to) {
        StringBuilder csv = new StringBuilder("Date,Task,Mode,Minutes,Started At\r\n");
        for (FocusSession s : history(userId, from)) {
            if (s.getStartedAt().toLocalDate().isAfter(to != null ? to : LocalDate.now())) continue;
            String taskTitle = "";
            if (s.getTaskId() != null) {
                taskTitle = taskRepository.findById(s.getTaskId())
                        .map(Task::getTitle).orElse("");
            }
            csv.append(escape(s.getStartedAt().toLocalDate().toString())).append(',')
               .append(escape(taskTitle)).append(',')
               .append(s.getMode()).append(',')
               .append(s.getDurationSeconds() / 60).append(',')
               .append(escape(s.getStartedAt().toString()))
               .append("\r\n");
        }
        return csv.toString();
    }

    private static String escape(String value) {
        String safe = value == null ? "" : value;
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n")) {
            return '"' + safe.replace("\"", "\"\"") + '"';
        }
        return safe;
    }

    private static SessionMode parseMode(String mode) {
        try {
            return mode == null ? SessionMode.FOCUS : SessionMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mode must be FOCUS or BREAK");
        }
    }
}
