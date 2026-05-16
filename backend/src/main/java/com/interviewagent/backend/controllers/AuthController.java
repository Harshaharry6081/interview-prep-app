package com.interviewagent.backend.controllers;

import com.interviewagent.backend.models.User;
import com.interviewagent.backend.repositories.UserRepository;
import com.interviewagent.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private final WebClient googleWebClient = WebClient.builder()
            .baseUrl("https://oauth2.googleapis.com")
            .build();

    /**
     * Dummy GET endpoint to satisfy GCP Load Balancer Health Check.
     * GCP caches the initial readiness probe path and aggressively GETs it.
     */
    @GetMapping("/google")
    public ResponseEntity<String> healthCheckHack() {
        return ResponseEntity.ok("OK");
    }

    /**
     * Google Sign-In endpoint.
     * Accepts the Google ID token from the frontend,
     * verifies it against Google's tokeninfo API,
     * auto-creates the user if new, and returns our own JWT.
     */
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");

        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing idToken"));
        }

        try {
            // Verify the token with Google
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenInfo = (Map<String, Object>) googleWebClient.get()
                    .uri("/tokeninfo?id_token=" + idToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (tokenInfo == null || tokenInfo.containsKey("error")) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid Google token"));
            }

            String email   = (String) tokenInfo.get("email");
            String name    = tokenInfo.containsKey("name") ? (String) tokenInfo.get("name") : email.split("@")[0];
            String picture = tokenInfo.containsKey("picture") ? (String) tokenInfo.get("picture") : "";

            // Find or create the user
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .email(email)
                        .username(name)
                        .passwordHash("GOOGLE_SSO") // no password for Google users
                        .roles(List.of("ROLE_USER"))
                        .createdAt(LocalDateTime.now())
                        .build();
                return userRepository.save(newUser);
            });

            String jwt = jwtUtil.generateToken(email);

            return ResponseEntity.ok(Map.of(
                    "token",    jwt,
                    "username", user.getUsername(),
                    "email",    email,
                    "picture",  picture
            ));

        } catch (Exception e) {
            log.error("Google login failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("message", "Google authentication failed"));
        }
    }

    /**
     * Standard Username/Password login for testing purposes to bypass Google OAuth.
     */
    @PostMapping("/test-login")
    public ResponseEntity<?> testLogin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing username or password"));
        }

        // For this hackathon/test setup, we simply allow any username to log in
        // and automatically provision an account if it doesn't exist.
        String email = username.toLowerCase().replace(" ", "") + "@test.com";

        try {
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .email(email)
                        .username(username)
                        .passwordHash("TEST_USER_HASH") // Mock password hash
                        .roles(List.of("ROLE_USER"))
                        .createdAt(LocalDateTime.now())
                        .build();
                return userRepository.save(newUser);
            });

            String jwt = jwtUtil.generateToken(email);

            return ResponseEntity.ok(Map.of(
                    "token",    jwt,
                    "username", user.getUsername(),
                    "email",    email,
                    "picture",  "https://api.dicebear.com/7.x/avataaars/svg?seed=" + username
            ));
        } catch (Exception e) {
            log.error("Test login failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("message", "Authentication failed"));
        }
    }
}
