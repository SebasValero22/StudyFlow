package com.iescamp.studyflow_api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Unauthenticated endpoints so self-hosted ("bring your own cloud") clients
 * can verify reachability and version before logging in.
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Value("${spring.application.name:StudyFlow_api}")
    private String appName;

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "status", "ok",
                "app", appName,
                "time", Instant.now().toString()
        );
    }
}
