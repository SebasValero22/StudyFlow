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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectCollaborationIntegrationTest {

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
    void sharedProjectTasksAreVisibleAndEditableByEditorsOnly() throws Exception {
        String owner = register("boss@test.com");
        String editor = register("teammate@test.com");
        String outsider = register("stranger@test.com");

        // Owner creates an area + project under it
        Resp areaResp = send(owner, "POST", "/api/subjects",
                json.writeValueAsString(Map.of("name", "Client Work", "color", "#B2F2BB",
                        "academicYear", "2025/2026", "active", true)));
        System.out.println("PROJ_DEBUG area-status=" + areaResp.status() + " area-body=" + areaResp.body());
        Integer areaId = json.readTree(areaResp.body()).path("lifeAreaId").asInt();
        Resp projectResp = send(owner, "POST", "/api/projects",
                json.writeValueAsString(Map.of("lifeAreaId", areaId, "name", "Acme Website")));
        System.out.println("PROJ_DEBUG areaId=" + areaId + " status=" + projectResp.status() + " body=" + projectResp.body());
        assertThat(projectResp.status()).isEqualTo(200);
        Integer projectId = json.readTree(projectResp.body()).path("projectId").asInt();
        assertThat(projectId).isPositive();

        // Outsider cannot see or touch the project
        assertThat(send(outsider, "DELETE", "/api/projects/" + projectId, null).status()).isEqualTo(404);

        // Only the owner can share; stranger's email must exist to be invited
        assertThat(send(editor, "POST", "/api/projects/" + projectId + "/members",
                json.writeValueAsString(Map.of("email", "x@x.com", "role", "EDITOR"))).status()).isEqualTo(404);

        Resp share = send(owner, "POST", "/api/projects/" + projectId + "/members",
                json.writeValueAsString(Map.of("email", "teammate@test.com", "role", "EDITOR")));
        assertThat(share.status()).isEqualTo(200);

        // Duplicate shares rejected
        assertThat(send(owner, "POST", "/api/projects/" + projectId + "/members",
                json.writeValueAsString(Map.of("email", "teammate@test.com", "role", "VIEWER"))).status())
                .isEqualTo(409);

        // OWNER role cannot be granted
        assertThat(send(owner, "POST", "/api/projects/" + projectId + "/members",
                json.writeValueAsString(Map.of("email", "stranger@test.com", "role", "OWNER"))).status())
                .isEqualTo(400);

        // Editor creates a task inside the shared project
        Resp taskResp = send(editor, "POST", "/api/tasks",
                json.writeValueAsString(Map.of("title", "Build landing page", "description", "",
                        "isCompleted", false, "priority", "HIGH", "projectId", projectId)));
        System.out.println("PROJ_DEBUG create-task status=" + taskResp.status() + " body=" + taskResp.body());
        assertThat(taskResp.status()).isEqualTo(200);
        Integer taskId = json.readTree(taskResp.body()).path("taskId").asInt();
        assertThat(json.readTree(taskResp.body()).path("projectId").asInt()).isEqualTo(projectId);

        // Everyone with access sees it in their list; outsider does not
        assertThat(json.readValue(send(owner, "GET", "/api/tasks", null).body(), List.class))
                .anySatisfy(t -> assertThat(((Map<?, ?>) t).get("title")).isEqualTo("Build landing page"));
        assertThat(json.readValue(send(editor, "GET", "/api/tasks", null).body(), List.class))
                .anySatisfy(t -> assertThat(((Map<?, ?>) t).get("title")).isEqualTo("Build landing page"));
        assertThat(json.readValue(send(outsider, "GET", "/api/tasks", null).body(), List.class)).isEmpty();

        // Editor can update it; outsider cannot even see it
        assertThat(send(editor, "PUT", "/api/tasks/" + taskId,
                json.writeValueAsString(Map.of("title", "Build landing page v2"))).status()).isEqualTo(200);
        assertThat(send(outsider, "GET", "/api/tasks/" + taskId, null).status()).isEqualTo(404);
        assertThat(send(outsider, "PUT", "/api/tasks/" + taskId,
                json.writeValueAsString(Map.of("title", "hax"))).status()).isEqualTo(404);

        // Member list shows roles; owner removes the editor; project disappears from editor's list
        List<?> members = json.readValue(send(owner, "GET", "/api/projects/" + projectId + "/members", null).body(),
                List.class);
        assertThat(members).hasSize(2);
        assertThat(send(owner, "DELETE", "/api/projects/" + projectId + "/members/"
                + json.readTree(share.body()).path("memberId").asInt(), null).status()).isEqualTo(200);
        // After leaving, the editor loses visibility of SHARED data but keeps his own task
        assertThat(json.readValue(send(editor, "GET", "/api/projects", null).body(), List.class)).isEmpty();
        assertThat(send(editor, "GET", "/api/tasks/" + taskId, null).status()).isEqualTo(200); // his own task
        assertThat(send(outsider, "GET", "/api/tasks/" + taskId, null).status()).isEqualTo(404);
    }
}
