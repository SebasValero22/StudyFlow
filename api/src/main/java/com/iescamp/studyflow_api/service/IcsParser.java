package com.iescamp.studyflow_api.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal RFC 5545 subset parser: enough for Outlook/Google/Fastmail exports.
 * Handles line unfolding, VEVENT blocks, DTSTART/DTEND in UTC, floating and
 * all-day (VALUE=DATE) forms. RRULE expansion is out of scope v1 - recurring
 * events import as their first occurrence.
 */
@Component
public class IcsParser {

    public record Event(String uid, LocalDateTime start, LocalDateTime end, String summary) {}

    public List<Event> parse(String ics) {
        List<Event> events = new ArrayList<>();
        if (ics == null || ics.isBlank()) return events;

        // Unfold continuation lines (CRLF followed by space/tab)
        StringBuilder unfolded = new StringBuilder();
        for (String rawLine : ics.split("\r?\n")) {
            if ((rawLine.startsWith(" ") || rawLine.startsWith("\t")) && unfolded.length() > 0) {
                unfolded.append(rawLine.substring(1));
            } else {
                if (unfolded.length() > 0) unfolded.append('\n');
                unfolded.append(rawLine);
            }
        }

        String uid = null, summary = null;
        LocalDateTime start = null, end = null;
        boolean inEvent = false;

        for (String line : unfolded.toString().split("\n")) {
            String trimmed = line.trim();
            if (trimmed.equalsIgnoreCase("BEGIN:VEVENT")) {
                inEvent = true;
                uid = null; summary = null; start = null; end = null;
            } else if (trimmed.equalsIgnoreCase("END:VEVENT")) {
                if (inEvent && start != null) {
                    events.add(new Event(uid, start,
                            end != null ? end : start.plusHours(1), summary));
                }
                inEvent = false;
            } else if (inEvent && trimmed.contains(":")) {
                int colon = trimmed.indexOf(':');
                String keyPart = trimmed.substring(0, colon);
                String value = trimmed.substring(colon + 1).trim();
                String key = keyPart.split(";")[0].toUpperCase();

                switch (key) {
                    case "UID" -> uid = value;
                    case "SUMMARY" -> summary = unescape(value);
                    case "DTSTART" -> start = parseDateTime(value);
                    case "DTEND" -> end = parseDateTime(value);
                    default -> { /* DURATION, RRULE, etc.: skipped in v1 */ }
                }
            }
        }
        return events;
    }

    private LocalDateTime parseDateTime(String value) {
        try {
            // UTC form: 20260901T140000Z
            if (value.endsWith("Z")) {
                return LocalDateTime.parse(value.substring(0, value.length() - 1),
                        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
            }
            // All-day form: 20260901
            if (value.matches("\\d{8}")) {
                return LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay();
            }
            // Floating local form: 20260901T140000
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String unescape(String value) {
        return value.replace("\\,", ",").replace("\\;", ";").replace("\\n", "\n").replace("\\N", "\n");
    }
}
