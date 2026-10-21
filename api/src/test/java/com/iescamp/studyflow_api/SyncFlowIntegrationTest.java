package com.iescamp.studyflow_api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SyncFlowIntegrationTest {

    @Autowired
    private Environment env;

    private RestTemplate rest;
    private String base;
    private String token;
    private String otherToken;

    @BeforeEach
    void setUp() {
        rest = new RestTemplate();
        base = "http://localhost:" + env.getProperty("local.server.port");
        token = register("sync-user-" + System.nanoTime() + "@test.com");
        otherToken = register("sync-other-" + System.nanoTime() + "@test.com");
    }

    @Test
    void pushThenPullRoundtripRemapsNegativeIds() {
        Map<String, Object> area = Map.of(
                "localId", -1,
                "name", "Math",
                "color", "#ff0000",
                "active", true,
                "updatedAt", LocalDateTime.now().toString());
        Map<String, Object> task = Map.of(
                "localId", -5,
                "title", "Study integrals",
                "priority", "HIGH",
                "isCompleted", false,
                "lifeAreaId", -1,
                "updatedAt", LocalDateTime.now().toString());

        ResponseEntity<Map> push = post("/api/sync/push", Map.of(
                "lifeAreas", List.of(area),
                "tasks", List.of(task)), token);

        assertThat(push.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Integer> idMap = (Map<String, Integer>) push.getBody().get("idMap");
        assertThat(idMap).containsKeys("lifeArea:-1", "task:-5");
        Integer areaId = idMap.get("lifeArea:-1");
        Integer taskId = idMap.get("task:-5");
        assertThat(areaId).isPositive();
        assertThat(taskId).isPositive();

        ResponseEntity<Map> pull = get("/api/sync/pull", token);
        assertThat(pull.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> areas = (List<Map<String, Object>>) pull.getBody().get("lifeAreas");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) pull.getBody().get("tasks");

        Map<String, Object> pulledArea = areas.stream()
                .filter(a -> areaId.equals(a.get("lifeAreaId")))
                .findFirst().orElseThrow();
        assertThat(pulledArea.get("name")).isEqualTo("Math");

        Map<String, Object> pulledTask = tasks.stream()
                .filter(t -> taskId.equals(t.get("taskId")))
                .findFirst().orElseThrow();
        assertThat(pulledTask.get("title")).isEqualTo("Study integrals");
        assertThat(pulledTask.get("lifeAreaId")).isEqualTo(areaId);
        assertThat(pulledTask.get("deletedAt")).isNull();
    }

    @Test
    void softDeletePropagatesAsTombstone() {
        LocalDateTime now = LocalDateTime.now();
        ResponseEntity<Map> push = post("/api/sync/push", Map.of(
                "tasks", List.of(Map.of(
                        "localId", -1,
                        "title", "Will be deleted",
                        "priority", "LOW",
                        "isCompleted", false,
                        "updatedAt", now.toString()))), token);
        @SuppressWarnings("unchecked")
        Map<String, Integer> idMap = (Map<String, Integer>) push.getBody().get("idMap");
        Integer taskId = idMap.get("task:-1");

        // Tombstone push (client deleted locally)
        ResponseEntity<Map> tombstonePush = post("/api/sync/push", Map.of(
                "tasks", List.of(Map.of(
                        "taskId", taskId,
                        "title", "Will be deleted",
                        "priority", "LOW",
                        "isCompleted", false,
                        "updatedAt", LocalDateTime.now().plusSeconds(2).toString(),
                        "deletedAt", LocalDateTime.now().plusSeconds(2).toString()))), token);
        assertThat(tombstonePush.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> pull = get("/api/sync/pull", token);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) pull.getBody().get("tasks");
        Map<String, Object> tombstone = tasks.stream()
                .filter(t -> taskId.equals(t.get("taskId")))
                .findFirst().orElseThrow();
        assertThat(tombstone.get("deletedAt")).isNotNull();
    }

    @Test
    void lastWriteWinsKeepsNewerServerRow() {
        LocalDateTime older = LocalDateTime.now().minusMinutes(30);
        LocalDateTime newer = LocalDateTime.now().minusMinutes(1);

        ResponseEntity<Map> firstPush = post("/api/sync/push", Map.of(
                "tasks", List.of(Map.of(
                        "localId", -1,
                        "title", "Newer title",
                        "priority", "HIGH",
                        "isCompleted", false,
                        "updatedAt", newer.toString()))), token);
        assertThat(firstPush.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Integer> firstIdMap = (Map<String, Integer>) firstPush.getBody().get("idMap");
        Integer taskId = firstIdMap.get("task:-1");

        // Stale write must not clobber
        ResponseEntity<Map> stale = post("/api/sync/push", Map.of(
                "tasks", List.of(Map.of(
                        "taskId", taskId,
                        "title", "Stale title",
                        "priority", "LOW",
                        "isCompleted", true,
                        "updatedAt", older.toString()))), token);
        assertThat(stale.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> pull = get("/api/sync/pull", token);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) pull.getBody().get("tasks");
        Map<String, Object> row = tasks.stream()
                .filter(t -> taskId.equals(t.get("taskId")))
                .findFirst().orElseThrow();
        assertThat(row.get("title")).isEqualTo("Newer title");
        assertThat(row.get("isCompleted")).isEqualTo(false);
    }

    @Test
    void settingsRoundTripAndCrossUserIsolation() {
        post("/api/sync/push", Map.of(
                "settings", List.of(Map.of(
                        "settingKey", "theme",
                        "settingValue", "dark",
                        "updatedAt", LocalDateTime.now().toString()))), token);

        ResponseEntity<Map> mine = get("/api/sync/pull", token);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mySettings = (List<Map<String, Object>>) mine.getBody().get("settings");
        assertThat(mySettings)
                .anySatisfy(s -> {
                    assertThat(s.get("settingKey")).isEqualTo("theme");
                    assertThat(s.get("settingValue")).isEqualTo("dark");
                });

        ResponseEntity<Map> theirs = get("/api/sync/pull", otherToken);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> theirSettings = (List<Map<String, Object>>) theirs.getBody().get("settings");
        assertThat(theirSettings)
                .noneMatch(s -> "theme".equals(s.get("settingKey")) && "dark".equals(s.get("settingValue")));

        // Their push must not leak into my rows either
        ResponseEntity<Map> theirPush = post("/api/sync/push", Map.of(
                "tasks", List.of(Map.of(
                        "localId", -1,
                        "title", "Secret of other user",
                        "priority", "MEDIUM",
                        "isCompleted", false,
                        "updatedAt", LocalDateTime.now().toString()))), otherToken);
        assertThat(theirPush.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> myPull = get("/api/sync/pull", token);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> myTasks = (List<Map<String, Object>>) myPull.getBody().get("tasks");
        assertThat(myTasks).noneMatch(t -> "Secret of other user".equals(t.get("title")));
    }

    @Test
    void idempotentPushWithClientUuidDoesNotDuplicate() {
        String uuid = java.util.UUID.randomUUID().toString();
        Map<String, Object> task = Map.of(
                "localId", -1,
                "title", "Idempotent row",
                "priority", "HIGH",
                "isCompleted", false,
                "clientUuid", uuid,
                "updatedAt", LocalDateTime.now().toString());

        ResponseEntity<Map> first = post("/api/sync/push", Map.of("tasks", List.of(task), "protocolVersion", 2), token);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Integer> firstMap = (Map<String, Integer>) first.getBody().get("idMap");
        Integer taskId = firstMap.get("task:-1");
        assertThat(taskId).isPositive();

        // Re-push same clientUuid with a new negative localId — must map to same server row.
        Map<String, Object> retry = Map.of(
                "localId", -99,
                "title", "Idempotent row",
                "priority", "HIGH",
                "isCompleted", false,
                "clientUuid", uuid,
                "updatedAt", LocalDateTime.now().plusSeconds(1).toString());
        ResponseEntity<Map> second = post("/api/sync/push", Map.of("tasks", List.of(retry)), token);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Integer> secondMap = (Map<String, Integer>) second.getBody().get("idMap");
        assertThat(secondMap.get("task:-99")).isEqualTo(taskId);

        ResponseEntity<Map> pull = get("/api/sync/pull", token);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) pull.getBody().get("tasks");
        long matches = tasks.stream().filter(t -> taskId.equals(t.get("taskId"))).count();
        assertThat(matches).isEqualTo(1);
    }

    @Test
    void examsGradesRemindersRoundtripWithIdMap() {
        LocalDateTime now = LocalDateTime.now();
        ResponseEntity<Map> push = post("/api/sync/push", Map.of(
                "lifeAreas", List.of(Map.of(
                        "localId", -1,
                        "name", "Physics",
                        "color", "#00ff00",
                        "active", true,
                        "updatedAt", now.toString())),
                "exams", List.of(Map.of(
                        "localId", -10,
                        "lifeAreaId", -1,
                        "nameExam", "Midterm",
                        "examType", "WRITTEN",
                        "examDate", "2026-06-01",
                        "updatedAt", now.toString())),
                "grades", List.of(Map.of(
                        "localId", -20,
                        "lifeAreaId", -1,
                        "concept", "Homework 1",
                        "score", 8.5,
                        "weight", 20.0,
                        "gradeDate", "2026-05-01",
                        "updatedAt", now.toString())),
                "reminders", List.of(Map.of(
                        "localId", -30,
                        "message", "Study for midterm",
                        "dateTime", now.plusDays(2).toString(),
                        "updatedAt", now.toString()))), token);

        assertThat(push.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Integer> idMap = (Map<String, Integer>) push.getBody().get("idMap");
        assertThat(idMap).containsKeys("lifeArea:-1", "exam:-10", "grade:-20", "reminder:-30");

        ResponseEntity<Map> pull = get("/api/sync/pull", token);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> exams = (List<Map<String, Object>>) pull.getBody().get("exams");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> grades = (List<Map<String, Object>>) pull.getBody().get("grades");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reminders = (List<Map<String, Object>>) pull.getBody().get("reminders");

        Integer examId = idMap.get("exam:-10");
        Integer areaId = idMap.get("lifeArea:-1");
        assertThat(exams).anySatisfy(e -> {
            assertThat(e.get("examId")).isEqualTo(examId);
            assertThat(e.get("lifeAreaId")).isEqualTo(areaId);
            assertThat(e.get("nameExam")).isEqualTo("Midterm");
        });
        assertThat(grades).anySatisfy(g -> assertThat(g.get("concept")).isEqualTo("Homework 1"));
        assertThat(reminders).anySatisfy(r -> assertThat(r.get("message")).isEqualTo("Study for midterm"));
        assertThat(pull.getBody().get("protocolVersion")).isEqualTo(2);
    }

    @Test
    void invalidRowIsRejectedWithoutBreakingBatch() {
        LocalDateTime now = LocalDateTime.now();
        ResponseEntity<Map> push = post("/api/sync/push", Map.of(
                "tasks", List.of(
                        Map.of("localId", -1, "title", "Valid task", "priority", "HIGH",
                                "isCompleted", false, "updatedAt", now.toString()),
                        Map.of("localId", -2, "title", "   ", "priority", "LOW",
                                "isCompleted", false, "updatedAt", now.toString()))), token);

        assertThat(push.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Integer> idMap = (Map<String, Integer>) push.getBody().get("idMap");
        assertThat(idMap).containsKey("task:-1");
        assertThat(idMap).doesNotContainKey("task:-2");

        @SuppressWarnings("unchecked")
        Map<String, String> rejected = (Map<String, String>) push.getBody().get("rejected");
        assertThat(rejected).containsKey("task:-2");
        assertThat(rejected.get("task:-2")).contains("title");

        // The rejected row must not exist server-side.
        ResponseEntity<Map> pull = get("/api/sync/pull", token);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) pull.getBody().get("tasks");
        assertThat(tasks).noneMatch(t -> "   ".equals(t.get("title")));
    }

    // ---- helpers ------------------------------------------------------

    private String register(String email) {
        ResponseEntity<Map> reg = rest.postForEntity(base + "/api/auth/register",
                Map.of("name", "Sync", "email", email, "password", "secret123"), Map.class);
        assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) reg.getBody().get("token");
    }

    private HttpEntity<Object> entity(Object body, String bearer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (bearer != null) headers.setBearerAuth(bearer);
        return new HttpEntity<>(body, headers);
    }

    private ResponseEntity<Map> post(String path, Object body, String bearer) {
        return rest.exchange(base + path, HttpMethod.POST, entity(body, bearer), Map.class);
    }

    private ResponseEntity<Map> get(String path, String bearer) {
        return rest.exchange(base + path, HttpMethod.GET, entity(null, bearer), Map.class);
    }
}
