
package com.fileshare.filesharing;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final Map<String, String> users = new ConcurrentHashMap<>();

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> data) {

        String username = data.get("username");
        String password = data.get("password");

        if (username == null || username.isBlank()
                || password == null || password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Username is required and password must be at least 6 characters"));
        }

        String encodedPassword = passwordEncoder.encode(password);

        if (users.putIfAbsent(username, encodedPassword) != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Username already exists"));
        }

        return ResponseEntity.ok(
                Map.of("message", "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> data,
            HttpSession session) {

        String username = data.get("username");
        String password = data.get("password");

        String storedPassword =
                username == null ? null : users.get(username);

        if (password == null || storedPassword == null
                || !passwordEncoder.matches(password, storedPassword)) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Invalid username or password"));
        }

        session.setAttribute("loggedInUser", username);

        return ResponseEntity.ok(
                Map.of("message", "Login successful",
                        "username", username));
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(HttpSession session) {


        String username = (String) session.getAttribute("loggedInUser");

        if (username == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Not logged in"));
        }

        return ResponseEntity.ok(Map.of("username", username));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully"));
    }
}
