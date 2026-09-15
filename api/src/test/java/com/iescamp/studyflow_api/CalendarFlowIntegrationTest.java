package com.iescamp.studyflow_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CalendarFlowIntegrationTest {

    @Autowired
    private Environment env;

    private HttpClientLike http;
    private String base;
    private HttpServer icsServer;
    private int icsPort;
    private LocalDate busyDay; // tomorrow - inside the /load rolling window
    private final ObjectMapper json = new ObjectMapper();
    private final AtomicInteger hits = new AtomicInteger();

    record Resp(int status, String body) {}

    private static final String ICS_TEMPLATE = """
            BEGIN:VCALENDAR
            VERSION:2.0
            BEGIN:VEVENT
            UID:morning-standup@work
            DTSTART:%s
            DTEND:%s
            SUMMARY:Standup
            END:VEVENT
            BEGIN:VEVENT
            UID:client-review@work
            DTSTART:%s
            DTEND:%s
            SUMMARY:Client architecture review
            END:VEVENT
            END:VCALENDAR
            """;

    private static String stamp(LocalDate d, int h, int m) {
        return d.format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("T%02d%02d00Z", h, m);
    }

    @BeforeEach
    void setUp() throws Exception {
        http = new HttpClientLike();
        base = "http://localhost:" + env.getProperty("local.server.port");
        busyDay = LocalDate.now().plusDays(1); // inside /load's 7-day rolling window

        // Embedded ICS origin - reachable thanks to studyflow.calendar.allow-private=true (test profile)
        icsServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        icsServer.createContext("/calendar.ics", exchange -> {
            hits.incrementAndGet();
            byte[] bytes = ICS_TEMPLATE.formatted(
                    stamp(busyDay, 9, 0), stamp(busyDay, 9, 15),
                    stamp(busyDay, 14, 0), stamp(busyDay, 16, 0))
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/calendar");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        icsServer.start();
        icsPort = icsServer.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        icsServer.stop(0);
    }

    private static class HttpClientLike {
        java.net.http.HttpClient delegate = java.net.http.HttpClient.newHttpClient();

        Resp send(String token, String method, String url, String jsonBody) throws Exception {
            java.net.http.HttpRequest.Builder b =
                    java.net.http.HttpRequest.newBuilder(java.net.URI.create(url)).header("Content-Type", "application/json");
            if (token != null) b.header("Authorization", "Bearer " + token);
            b.method(method, jsonBody == null ? java.net.http.HttpRequest.BodyPublishers.noBody()
                    : java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody));
            java.net.http.HttpResponse<String> r =
                    delegate.send(b.build(), java.net.http.HttpResponse.BodyHandlers.ofString());
            return new Resp(r.statusCode(), r.body());
        }
    }

    private String register(String email) throws Exception {
        Resp r = http.send(null, "POST", base + "/api/auth/register",
                json.writeValueAsString(Map.of("name", email.split("@")[0], "email", email, "password", "pass123")));
        assertThat(r.status()).isEqualTo(200);
        return json.readTree(r.body()).path("token").asText();
    }

    @Test
    void feedLifecycleSyncAndBusyQuery() throws Exception {
        String token = register("calendar@test.com");

        // Create a feed pointing at the embedded ICS server
        Resp create = http.send(token, "POST", base + "/api/calendar/feeds",
                json.writeValueAsString(Map.of("name", "Work calendar",
                        "url", "http://127.0.0.1:" + icsPort + "/calendar.ics")));
        assertThat(create.status()).isEqualTo(200);
        Integer feedId = json.readTree(create.body()).path("feedId").asInt();
        assertThat(feedId).isPositive();

        // Sync pulls both events in
        Resp sync = http.send(token, "POST", base + "/api/calendar/feeds/" + feedId + "/sync", null);
        assertThat(sync.status()).isEqualTo(200);
        assertThat(json.readTree(sync.body()).path("events").asInt()).isEqualTo(2);

        // Busy query returns the merged blocks for the window
        Resp busy = http.send(token, "GET", base + "/api/calendar/busy?from="
                + busyDay + "&to=" + busyDay, null);
        assertThat(busy.status()).isEqualTo(200);
        List<?> blocks = json.readValue(busy.body(), List.class);
        assertThat(blocks).hasSize(2);

        // Load endpoint aggregates hours per day: 15min + 2h = 2.25h on the busy day
        Resp load = http.send(token, "GET", base + "/api/calendar/load?days=7", null);
        assertThat(load.status()).isEqualTo(200);
        assertThat(json.readTree(load.body()).path(busyDay.toString()).asDouble()).isEqualTo(2.25);

        // Ownership: another account cannot delete someone else's feed (404, no leak)
        String other = register("othercal@test.com");
        assertThat(http.send(other, "DELETE", base + "/api/calendar/feeds/" + feedId, null).status())
                .isEqualTo(404);

        // Owner cleanup works and cascades to blocks
        assertThat(http.send(token, "DELETE", base + "/api/calendar/feeds/" + feedId, null).status())
                .isEqualTo(200);
        Resp busyAfter = http.send(token, "GET", base + "/api/calendar/busy?from="
                + busyDay + "&to=" + busyDay, null);
        assertThat(json.readValue(busyAfter.body(), List.class)).isEmpty();
    }

    @Test
    void rejectsInvalidFeedUrls() throws Exception {
        String token = register("guard@test.com");

        Resp ftp = http.send(token, "POST", base + "/api/calendar/feeds",
                json.writeValueAsString(Map.of("name", "evil", "url", "ftp://example.com/x.ics")));
        assertThat(ftp.status()).isEqualTo(400);

        Resp noHost = http.send(token, "POST", base + "/api/calendar/feeds",
                json.writeValueAsString(Map.of("name", "evil", "url", "not-a-url")));
        assertThat(noHost.status()).isEqualTo(400);

        Resp blankName = http.send(token, "POST", base + "/api/calendar/feeds",
                json.writeValueAsString(Map.of("name", " ", "url", "https://example.com/ok.ics")));
        assertThat(blankName.status()).isEqualTo(400);
    }
}
