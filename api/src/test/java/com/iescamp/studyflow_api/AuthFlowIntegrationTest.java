package com.iescamp.studyflow_api;

import com.iescamp.studyflow_api.model.User;
import com.iescamp.studyflow_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowIntegrationTest {

    @Autowired
    private Environment env;

    @Autowired
    private UserRepository userRepository;

    private RestTemplate rest;
    private String base;

    @BeforeEach
    void setUp() {
        rest = new RestTemplate();
        base = "http://localhost:" + env.getProperty("local.server.port");
    }

    @Test
    void registerIssuesTokenAndSecuresEndpoints() {
        // 1. Public register returns token + user
        ResponseEntity<Map> reg = rest.postForEntity(base + "/api/auth/register",
                Map.of("name", "Sebas", "email", "sebas@test.com", "password", "secret123"), Map.class);
        assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) reg.getBody().get("token")).isNotBlank();
        Map user = (Map) reg.getBody().get("user");
        assertThat(user.get("email")).isEqualTo("sebas@test.com");

        String token = (String) reg.getBody().get("token");

        // 2. Protected endpoint rejected without token -> 401
        try {
            rest.getForEntity(base + "/api/tasks", String.class);
            org.assertj.core.api.Assertions.fail("Expected 401 for anonymous /api/tasks");
        } catch (HttpStatusCodeException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        // 3. Same endpoint accepted with Bearer token -> 200
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> authed = rest.exchange(base + "/api/tasks", HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
        assertThat(authed.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 4. Login again with the BCrypt-registered password
        ResponseEntity<Map> login = rest.postForEntity(base + "/api/auth/login",
                Map.of("email", "sebas@test.com", "password", "secret123"), Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) login.getBody().get("token")).isNotBlank();

        // 5. Wrong password -> 401
        try {
            rest.postForEntity(base + "/api/auth/login",
                    Map.of("email", "sebas@test.com", "password", "wrong"), Map.class);
            org.assertj.core.api.Assertions.fail("Expected 401 for wrong password");
        } catch (HttpStatusCodeException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    void legacyPlaintextPasswordIsRehashedOnNextLogin() throws Exception {
        // Simulate a pre-existing legacy user with a plaintext password
        User legacy = new User();
        legacy.setUserName("legacy");
        legacy.setEmail("legacy@test.com");
        legacy.setPassword("plaintext-pass");
        legacy.setActive(true);
        userRepository.save(legacy);

        // Login through the public endpoint
        ResponseEntity<Map> login = rest.postForEntity(base + "/api/auth/login",
                Map.of("email", "legacy@test.com", "password", "plaintext-pass"), Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Stored hash must now be BCrypt
        User stored = userRepository.findByEmail("legacy@test.com").orElseThrow();
        assertThat(stored.getPassword()).startsWith("$2");
    }
}
