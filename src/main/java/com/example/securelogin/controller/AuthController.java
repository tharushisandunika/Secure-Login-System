package com.example.securelogin.controller;

import com.example.securelogin.dto.*;
import com.example.securelogin.model.User;
import com.example.securelogin.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController              // This class handles HTTP requests and returns JSON
@RequestMapping("/api/auth") // All URLs in this class start with /api/auth
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final TotpService totpService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            User user = userService.authenticate(req.getUsername(), req.getPassword());

            // If MFA is enabled, verify TOTP code
            if (user.isMfaEnabled()) {
                if (req.getTotpCode() == null || req.getTotpCode().trim().isEmpty()) {
                    return ResponseEntity.ok(Map.of(
                            "success", false,
                            "mfaRequired", true,
                            "message", "MFA verification required."
                    ));
                }
                if (!totpService.verifyCode(user.getMfaSecret(), req.getTotpCode())) {
                    return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid MFA code."));
                }
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

    // GET /api/auth/validate (called by dashboard to check if token is still valid)
    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (jwtService.isTokenValid(token)) {
            String username = jwtService.extractUsername(token);
            User user = userService.findByUsername(username);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", user.getUsername(),
                    "role", user.getRole(),
                    "mfaEnabled", user.isMfaEnabled()
            ));
        }
        return ResponseEntity.status(401).body(Map.of("valid", false));
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }

    // GET /api/auth/mfa/setup - Initiate MFA setup
    @GetMapping("/mfa/setup")
    public ResponseEntity<?> setupMfa() {
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.findByUsername(username);
            if (user.isMfaEnabled()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "MFA is already enabled."));
            }
            String secret = userService.generateMfaSecret(user);
            String qrCodeUrl = totpService.getQrCodeUrl(user.getUsername(), secret);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "secret", secret,
                    "qrCodeUrl", qrCodeUrl
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // POST /api/auth/mfa/verify - Confirm and enable MFA
    @PostMapping("/mfa/verify")
    public ResponseEntity<?> verifyMfa(@RequestBody Map<String, String> body) {
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.findByUsername(username);
            String code = body.get("code");
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Verification code is required."));
            }
            if (!totpService.verifyCode(user.getMfaSecret(), code)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid verification code."));
            }
            userService.enableMfa(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "MFA has been enabled successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // POST /api/auth/mfa/disable - Disable MFA
    @PostMapping("/mfa/disable")
    public ResponseEntity<?> disableMfa(@RequestBody Map<String, String> body) {
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.findByUsername(username);
            String code = body.get("code");
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Verification code is required."));
            }
            if (!totpService.verifyCode(user.getMfaSecret(), code)) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid verification code."));
            }
            userService.disableMfa(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "MFA has been disabled successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
