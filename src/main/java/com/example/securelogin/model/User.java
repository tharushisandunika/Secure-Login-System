package com.example.securelogin.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String role;

    @Builder.Default
    private boolean accountNonLocked = true;

    @Builder.Default
    private int failedAttempts = 0;

    private java.time.LocalDateTime lockTime;

    @Builder.Default
    private boolean mfaEnabled = false;

    private String mfaSecret;
}