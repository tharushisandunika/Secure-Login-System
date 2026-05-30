package com.example.securelogin.controller;

import com.example.securelogin.dto.*;
import com.example.securelogin.model.User;
import com.example.securelogin.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController              // This class handles HTTP requests and returns JSON
@RequestMapping("/api/auth") // All URLs in this class start with /api/auth
@RequiredArgsConstructor
@CrossOrigin(origins = "*")  // Allow frontend HTML files to call this API
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            User user = userService.registerUser(req.getUsername(), req.getEmail(), req.getPassword());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Account created successfully!",
                    "username", user.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            User user = userService.findByUsername(req.getUsername());
            if (!userService.verifyPassword(req.getPassword(), user.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid credentials"));
            }
            String token = jwtService.generateToken(user.getUsername());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", token,
                    "username", user.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // GET /api/auth/validate  (called by dashboard to check if token is still valid)
    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (jwtService.isTokenValid(token)) {
            return ResponseEntity.ok(Map.of("valid", true, "username", jwtService.extractUsername(token)));
        }
        return ResponseEntity.status(401).body(Map.of("valid", false));
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // JWT is stateless — logout is handled on frontend by deleting the token
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }
}
