package com.iescamp.studyflow_api;

import com.iescamp.studyflow_api.service.IcsParser;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IcsParserTest {

    private final IcsParser parser = new IcsParser();

    private static final String FIXTURE = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Test//EN
            BEGIN:VEVENT
            UID:evt-001@calendar.test
            DTSTAMP:20260824T080000Z
            DTSTART:20260901T140000Z
            DTEND:20260901T153000Z
            SUMMARY:Client call \\, Q3 planning
            END:VEVENT
            BEGIN:VEVENT
            UID:evt-002@calendar.test
            DTSTART;VALUE=DATE:20260902
            DTEND;VALUE=DATE:20260903
            SUMMARY:All-day conference
            END:VEVENT
            BEGIN:VEVENT
            UID:evt-003@calendar.test
            DTSTART:20260903T090000
            DTEND:20260903T100000
            SUMMARY:Floating local meeting
            END:VEVENT
            END:VCALENDAR
            """;

    @Test
    void parsesUtcAllDayAndFloatingEvents() {
        List<IcsParser.Event> events = parser.parse(FIXTURE);

        assertThat(events).hasSize(3);

        IcsParser.Event utc = events.get(0);
        assertThat(utc.uid()).isEqualTo("evt-001@calendar.test");
        assertThat(utc.start()).isEqualTo(LocalDateTime.of(2026, 9, 1, 14, 0));
        assertThat(utc.end()).isEqualTo(LocalDateTime.of(2026, 9, 1, 15, 30));
        assertThat(utc.summary()).isEqualTo("Client call , Q3 planning");

        IcsParser.Event allDay = events.get(1);
        assertThat(allDay.start()).isEqualTo(LocalDateTime.of(2026, 9, 2, 0, 0));

        IcsParser.Event floating = events.get(2);
        assertThat(floating.start()).isEqualTo(LocalDateTime.of(2026, 9, 3, 9, 0));
        // Missing DTEND defaults to +1h
        assertThat(floating.end()).isEqualTo(LocalDateTime.of(2026, 9, 3, 10, 0));
    }

    @Test
    void unfoldsFoldedLines() {
        String folded = """
                BEGIN:VEVENT
                UID:folded@test
                DTSTART:20260910T100000Z
                SUMMARY:This summary was split across two lines because it is very long indeed
                  and continues here
                END:VEVENT
                END:VCALENDAR
                """;
        List<IcsParser.Event> events = parser.parse("BEGIN:VCALENDAR\n" + folded);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).summary())
                .contains("split across two lines")
                .contains("continues here");
    }

    @Test
    void toleratesGarbage() {
        assertThat(parser.parse(null)).isEmpty();
        assertThat(parser.parse("")).isEmpty();
        assertThat(parser.parse("not an ics at all")).isEmpty();
    }
}
