package com.example.securelogin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Welcome to the Secure Admin Panel. This content is restricted to users with the ROLE_ADMIN role.",
                "activeConnections", 3,
                "serverUptime", "48 hours, 12 minutes",
                "databaseStatus", "ONLINE"
        ));
    }
}
