package com.iescamp.studyflow_api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JournalFlowIntegrationTest {

    private static final String MOCK_CLEANED =
            "# Journal Entry\\n\\n- [ ] Review captured thoughts";

    @Autowired
    private Environment env;

    private RestTemplate rest;
    private final java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
    private final com.fasterxml.jackson.databind.ObjectMapper json = new com.fasterxml.jackson.databind.ObjectMapper();
    private String base;

    @BeforeEach
    void setUp() {
        rest = new RestTemplate();
        base = "http://localhost:" + env.getProperty("local.server.port");
    }

    private String registerOther() throws Exception {
        var regReq = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(base + "/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(
                        "{\"name\":\"Other\",\"email\":\"journal-other@test.com\",\"password\":\"pass123\"}"))
                .build();
        var regResp = client.send(regReq, java.net.http.HttpResponse.BodyHandlers.ofString());
        assertThat(regResp.statusCode()).isEqualTo(200);
        return json.readTree(regResp.body()).path("token").asText();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void journalIngestRequiresAuthAndStoresCleanedEntry() throws Exception {
        // 0. Anonymous access is rejected
        try {
            rest.getForEntity(base + "/api/journal", String.class);
            fail("Expected 401 for anonymous /api/journal");
        } catch (HttpStatusCodeException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        // 1. Register + grab token
        ResponseEntity<Map> reg = rest.postForEntity(base + "/api/auth/register",
                Map.of("name", "Journer", "email", "journal@test.com", "password", "pass123"), Map.class);
        assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.OK);
        HttpHeaders auth = new HttpHeaders();
        String token = (String) reg.getBody().get("token");
        auth.setBearerAuth(token);

        // Probe: plain GET with same bearer (multipart-free control experiment)
        try {
            ResponseEntity<String> probe = rest.exchange(base + "/api/tasks", HttpMethod.GET,
                    new HttpEntity<>(auth), String.class);
            assertThat(probe.getStatusCode()).isEqualTo(HttpStatus.OK);
        } catch (HttpStatusCodeException e) {
            fail("Bearer token should be valid for plain requests: " + e.getStatusCode());
        }

        // 2. Ingest a text entry via raw HttpClient (multipart built by hand)
        String boundary = "----journalboundary";
        String body = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"text\"\r\n\r\n"
                + "Umm so today I studied JPA mappings and like it was hard but good\r\n"
                + "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"durationSeconds\"\r\n\r\n"
                + "42\r\n"
                + "--" + boundary + "--\r\n";

        java.net.http.HttpRequest ingestReq = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(base + "/api/journal/ingest"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                .build();

        java.net.http.HttpResponse<String> ingestResp =
                java.net.http.HttpClient.newHttpClient().send(ingestReq, java.net.http.HttpResponse.BodyHandlers.ofString());
        assertThat(ingestResp.statusCode()).isEqualTo(HttpStatus.OK.value());
        @SuppressWarnings("unchecked")
        Map<String, Object> ingested = new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(ingestResp.body(), Map.class);
        assertThat((String) ingested.get("rawTranscript")).contains("JPA");
        assertThat((String) ingested.get("transcript")).isNotBlank();

        // 3. Listing shows exactly our entry, newest first, scoped to this user
        ResponseEntity<List> list = rest.exchange(base + "/api/journal", HttpMethod.GET,
                new HttpEntity<>(auth), List.class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody()).hasSize(1);

        // 4. Promote an action item into a real task (the reflection -> execution bridge)
        ResponseEntity<Map> promote = rest.exchange(
                base + "/api/journal/" + ingested.get("entryId") + "/actions",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("text", "Review RA2 core components"), auth),
                Map.class);
        assertThat(promote.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(promote.getBody().get("title")).isEqualTo("Review RA2 core components");

        ResponseEntity<List> tasks = rest.exchange(base + "/api/tasks", HttpMethod.GET,
                new HttpEntity<>(auth), List.class);
        assertThat(tasks.getBody()).anySatisfy(t -> {
            Map<?, ?> task = (Map<?, ?>) t;
            assertThat(task.get("title")).isEqualTo("Review RA2 core components");
        });

        // 5. Blank action text rejected
        try {
            rest.exchange(base + "/api/journal/" + ingested.get("entryId") + "/actions",
                    HttpMethod.POST,
                    new HttpEntity<>(Map.of("text", "   "), auth),
                    String.class);
            fail("Expected 400 for blank action text");
        } catch (HttpStatusCodeException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        // 6. Audio upload is stored and streamed back only to the owner
        byte[] audioBytes = "fake-webm-audio-bytes-for-test".getBytes();
        var audioBoundary = "----journalaudio";
        var audioBody = new StringBuilder();
        audioBody.append("--").append(audioBoundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"text\"\r\n\r\n")
                .append("voice note with attachment\r\n");
        audioBody.append("--").append(audioBoundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"audio\"; filename=\"note.webm\"\r\n")
                .append("Content-Type: audio/webm\r\n\r\n");
        var bodyBytes = new java.io.ByteArrayOutputStream();
        bodyBytes.writeBytes(audioBody.toString().getBytes());
        bodyBytes.writeBytes(audioBytes);
        bodyBytes.writeBytes(("\r\n--" + audioBoundary + "--\r\n").getBytes());

        var audioReq = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(base + "/api/journal/ingest"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + audioBoundary)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(bodyBytes.toByteArray()))
                .build();
        var audioResp = client.send(audioReq, java.net.http.HttpResponse.BodyHandlers.ofString());
        assertThat(audioResp.statusCode()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, Object> audioEntry = json.readValue(audioResp.body(), Map.class);
        assertThat((String) audioEntry.get("audioPath")).isNotBlank();

        // Owner can fetch the exact bytes back
        var fetchReq = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(base + "/api/journal/" + audioEntry.get("entryId") + "/audio"))
                .header("Authorization", "Bearer " + token)
                .GET().build();
        var fetched = client.send(fetchReq, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
        assertThat(fetched.statusCode()).isEqualTo(200);
        assertThat(fetched.body()).isEqualTo(audioBytes);

        // Other users cannot fetch someone else's voice note
        String tokenOther = registerOther();
        var forbidden = client.send(java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(base + "/api/journal/" + audioEntry.get("entryId") + "/audio"))
                        .header("Authorization", "Bearer " + tokenOther)
                        .GET().build(),
                java.net.http.HttpResponse.BodyHandlers.ofString());
        assertThat(forbidden.statusCode()).isEqualTo(404);

        // Clean up the audio entry as well
        var delReq = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(base + "/api/journal/" + audioEntry.get("entryId")))
                .header("Authorization", "Bearer " + token)
                .DELETE().build();
        assertThat(client.send(delReq, java.net.http.HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(200);

        // 6. Delete works and returns 200
        Integer id = (Integer) ingested.get("entryId");
        ResponseEntity<String> del = rest.exchange(base + "/api/journal/" + id, HttpMethod.DELETE,
                new HttpEntity<>(auth), String.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<List> after = rest.exchange(base + "/api/journal", HttpMethod.GET,
                new HttpEntity<>(auth), List.class);
        assertThat(after.getBody()).isEmpty();
    }
}
