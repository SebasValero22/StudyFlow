package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.model.BusyBlock;
import com.iescamp.studyflow_api.model.CalendarFeed;
import com.iescamp.studyflow_api.security.CurrentUserProvider;
import com.iescamp.studyflow_api.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private CurrentUserProvider currentUser;

    public record FeedRequest(String name, String url) {}

    @PostMapping("/feeds")
    public ResponseEntity<?> addFeed(@RequestBody FeedRequest request) {
        try {
            return ResponseEntity.ok(calendarService.addFeed(
                    currentUser.getUserId(), request.name(), request.url()));
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Could not create feed");
        }
    }

    @GetMapping("/feeds")
    public List<CalendarFeed> listFeeds() {
        return calendarService.listFeeds(currentUser.getUserId());
    }

    @DeleteMapping("/feeds/{id}")
    public ResponseEntity<?> deleteFeed(@PathVariable Integer id) {
        try {
            calendarService.deleteFeed(currentUser.getUserId(), id);
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (ResponseStatusException rse) {
            throw rse;
        }
    }

    @PostMapping("/feeds/{id}/sync")
    public ResponseEntity<?> syncFeed(@PathVariable Integer id) {
        try {
            int imported = calendarService.syncFeed(currentUser.getUserId(), id);
            return ResponseEntity.ok(Map.of("events", imported));
        } catch (ResponseStatusException rse) {
            throw rse;
        }
    }

    @GetMapping("/busy")
    public List<BusyBlock> busy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return calendarService.busyBetween(currentUser.getUserId(),
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    /** Convenience endpoint for dashboards: busy hours per day, next N days. */
    @GetMapping("/load")
    public Map<String, Double> load(@RequestParam(defaultValue = "7") Integer days) {
        int safeDays = Math.min(Math.max(days, 1), 30);
        Map<LocalDate, Double> hours =
                calendarService.busyHoursPerDay(currentUser.getUserId(), LocalDate.now(), safeDays);
        return hours.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        e -> e.getKey().toString(), Map.Entry::getValue,
                        (a, b) -> a, java.util.LinkedHashMap::new));
    }
}
