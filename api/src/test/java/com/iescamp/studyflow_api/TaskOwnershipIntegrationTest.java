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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Uses raw java.net.http.HttpClient: RestTemplate's underlying
 * HttpURLConnection has proven unreliable with Authorization headers
 * across sequential requests on JDK 26.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskOwnershipIntegrationTest {

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

    private String registerAndGetToken(String email) throws Exception {
        Resp r = send(null, "POST", "/api/auth/register",
                json.writeValueAsString(Map.of("name", email.split("@")[0], "email", email, "password", "pass123")));
        assertThat(r.status()).isEqualTo(200);
        return json.readTree(r.body()).path("token").asText();
    }

    @Test
    void tasksAndAreasAreInvisibleAcrossAccounts() throws Exception {
        String tokenA = registerAndGetToken("owner@test.com");
        String tokenB = registerAndGetToken("intruder@test.com");

        // A creates a LifeArea
        Resp area = send(tokenA, "POST", "/api/subjects",
                json.writeValueAsString(Map.of("name", "A-only Area", "color", "#FFB7B2",
                        "academicYear", "2025/2026", "active", true)));
        assertThat(area.status()).isEqualTo(200);
        Integer areaId = json.readTree(area.body()).path("lifeAreaId").asInt();

        // A creates a Task - even with a spoofed userId, ownership must come from the JWT
        Integer spoofedUserId = 999999;
        Resp created = send(tokenA, "POST", "/api/tasks",
                json.writeValueAsString(Map.of("title", "Secret Task", "description", "",
                        "start_date", "2026-09-01", "due_date", "2026-09-02",
                        "isCompleted", false, "priority", "HIGH", "lifeAreaId", areaId,
                        "userId", spoofedUserId)));
        assertThat(created.status()).isEqualTo(200);
        Map<?, ?> task = json.readValue(created.body(), Map.class);
        Integer taskId = (Integer) task.get("taskId");
        assertThat(task.get("userId")).isNotEqualTo(spoofedUserId);

        // A sees their task; B sees nothing
        List<?> aTasks = json.readValue(send(tokenA, "GET", "/api/tasks", null).body(), List.class);
        List<?> bTasks = json.readValue(send(tokenB, "GET", "/api/tasks", null).body(), List.class);
        assertThat(aTasks).hasSize(1);
        assertThat(bTasks).isEmpty();

        // B cannot read, modify or delete A's task (404: existence never leaked)
        assertThat(send(tokenB, "GET", "/api/tasks/" + taskId, null).status()).isEqualTo(404);
        assertThat(send(tokenB, "PUT", "/api/tasks/" + taskId,
                json.writeValueAsString(Map.of("title", "hacked"))).status()).isEqualTo(404);
        assertThat(send(tokenB, "DELETE", "/api/tasks/" + taskId, null).status()).isEqualTo(404);

        // A still owns it end-to-end
        assertThat(send(tokenA, "GET", "/api/tasks/" + taskId, null).status()).isEqualTo(200);
    }
}
