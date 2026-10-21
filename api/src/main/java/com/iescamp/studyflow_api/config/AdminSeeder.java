package com.iescamp.studyflow_api.config;

import com.iescamp.studyflow_api.model.User;
import com.iescamp.studyflow_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ensures a local admin account exists on every boot (idempotent).
 * Intended for self-hosted / "bring your own cloud" setups where the owner
 * just wants to log in and start using the app immediately.
 *
 * NOTE: the default password is intentionally weak for local first-run
 * convenience. Change it right after login, or override via environment:
 *   STUDYFLOW_SEED_ADMIN_PASSWORD=<strong-password>
 */
@Component
@Order(10)
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String email;
    private final String password;

    public AdminSeeder(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${studyflow.seed.admin-enabled:true}") boolean enabled,
                       @Value("${studyflow.seed.admin-email:admin@mail.com}") String email,
                       @Value("${studyflow.seed.admin-password:123}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        userRepository.findByEmail(email).ifPresentOrElse(
                existing -> System.out.println("[seed] admin account already present: " + email),
                () -> {
                    User admin = new User();
                    admin.setUserName("admin");
                    admin.setEmail(email);
                    admin.setPassword(passwordEncoder.encode(password));
                    admin.setRegistrationDate(new java.util.Date());
                    admin.setActive(true);
                    userRepository.save(admin);
                    System.out.println("[seed] created admin account: " + email);
                });
    }
}
