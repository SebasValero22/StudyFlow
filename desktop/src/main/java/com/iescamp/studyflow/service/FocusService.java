package com.iescamp.studyflow.service;

import com.iescamp.studyflow.model.FocusSession;
import com.iescamp.studyflow.utils.JsonMapper;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Persists completed Animedoro cycles so focus time becomes timesheet data.
 */
public class FocusService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void recordSession(Integer taskId, int durationSeconds, boolean focus) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String payload = "{"
                + "\"taskId\":" + (taskId != null ? taskId : "null") + ","
                + "\"durationSeconds\":" + durationSeconds + ","
                + "\"mode\":\"" + (focus ? "FOCUS" : "BREAK") + "\","
                + "\"startedAt\":\"" + now.minusSeconds(durationSeconds).format(ISO) + "\","
                + "\"completed\":true}";

        HttpRequest request = ApiClient.prepareRequest("/focus/sessions")
                .POST(HttpRequest.BodyPublishers.ofString(payload)).build();

        HttpResponse<String> response = ApiClient.sendRequest(request);
        if (response.statusCode() != 200) {
            throw new Exception("Focus session rejected: HTTP " + response.statusCode());
        }
        JsonMapper.get().readValue(response.body(), FocusSession.class); // validates shape
    }
}
