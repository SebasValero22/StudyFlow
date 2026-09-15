package com.iescamp.studyflow_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FocusFlowIntegrationTest {

    @Autowired
    private Environment env;

    private HttpClient client;
    private String base;
    private final ObjectMapper json = new ObjectMapper();

    record Resp(int status, String body) {}

    @BeforeEach
    void setUp() {
        client = HttpClient.newHttpClient();
        base = "http://localhost:" + env.getProperty("local.server.port");
    }

    private Resp send(String token, String method, String path, String jsonBody) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(base + path))
                .header("Content-Type", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        b.method(method, jsonBody == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(jsonBody));
        HttpResponse<String> r = client.send(b.build(), HttpResponse.BodyHandlers.ofString());
        return new Resp(r.statusCode(), r.body());
    }

    private String register(String email) throws Exception {
        Resp r = send(null, "POST", "/api/auth/register",
                json.writeValueAsString(Map.of("name", email.split("@")[0], "email", email, "password", "pass123")));
        assertThat(r.status()).isEqualTo(200);
        return json.readTree(r.body()).path("token").asText();
    }

    @Test
    void sessionsAreRecordedReportedAndExportedPerUser() throws Exception {
        String tokenA = register("focusa@test.com");
        String tokenB = register("focusb@test.com");

        // A logs one 40-minute FOCUS session and one 20-minute BREAK
        Resp focus = send(tokenA, "POST", "/api/focus/sessions",
                json.writeValueAsString(Map.of("durationSeconds", 2400, "mode", "FOCUS")));
        Resp brk = send(tokenA, "POST", "/api/focus/sessions",
                json.writeValueAsString(Map.of("durationSeconds", 1200, "mode", "BREAK")));
        assertThat(focus.status()).isEqualTo(200);
        assertThat(brk.status()).isEqualTo(200);

        // Report: BREAK must not count toward focused time
        Resp report = send(tokenA, "GET", "/api/focus/report?from="
                + LocalDate.now().minusDays(1) + "&to=" + LocalDate.now(), null);
        assertThat(report.status()).isEqualTo(200);
        Map<?, ?> rep = json.readValue(report.body(), Map.class);
        assertThat(((Number) rep.get("totalFocusSeconds")).longValue()).isEqualTo(2400L);
        assertThat(((Number) rep.get("sessionCount")).longValue()).isEqualTo(1L);

        // Validation: nonsense durations rejected
        assertThat(send(tokenA, "POST", "/api/focus/sessions",
                json.writeValueAsString(Map.of("durationSeconds", -5))).status()).isEqualTo(400);
        assertThat(send(tokenA, "POST", "/api/focus/sessions",
                json.writeValueAsString(Map.of("durationSeconds", 99999999))).status()).isEqualTo(400);

        // B's report is empty - no cross-user leakage
        Map<?, ?> repB = json.readValue(
                send(tokenB, "GET", "/api/focus/report?from=" + LocalDate.now().minusDays(1)
                        + "&to=" + LocalDate.now(), null).body(), Map.class);
        assertThat(((Number) repB.get("totalFocusSeconds")).longValue()).isEqualTo(0L);
        assertThat(((Number) repB.get("sessionCount")).longValue()).isEqualTo(0L);

        // CSV timesheet: contains A's focus row with minutes, never B's data
        String from = LocalDate.now().minusDays(1).toString();
        String to = LocalDate.now().toString();
        Resp csv = send(tokenA, "GET", "/api/focus/timesheet.csv?from=" + from + "&to=" + to, null);
        assertThat(csv.status()).isEqualTo(200);
        assertThat(csv.body()).contains("Date,Task,Mode,Minutes,Started At");
        assertThat(csv.body()).contains(",FOCUS,40,");
        assertThat(csv.body()).doesNotContain("focusb");
        Resp csvB = send(tokenB, "GET", "/api/focus/timesheet.csv?from=" + from + "&to=" + to, null);
        assertThat(csvB.body().lines().count()).isEqualTo(1); // header only
    }
}
