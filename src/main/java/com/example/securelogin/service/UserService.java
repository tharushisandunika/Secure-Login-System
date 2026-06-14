package com.example.securelogin.service;

import com.example.securelogin.model.User;
import com.example.securelogin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor  // Lombok: auto-creates constructor for all final fields
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // Injected from SecurityConfig
    private final TotpService totpService;

    public String generateMfaSecret(User user) {
        String secret = totpService.generateSecretKey();
        user.setMfaSecret(secret);
        userRepository.save(user);
        return secret;
    }

    public void enableMfa(User user) {
        user.setMfaEnabled(true);
        userRepository.save(user);
    }

    public void disableMfa(User user) {
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);
    }

    public User registerUser(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username))
            throw new RuntimeException("Username already taken");
        if (userRepository.existsByEmail(email))
            throw new RuntimeException("Email already registered");

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))  // Hash the password!
                .role("ROLE_USER")
                .accountNonLocked(true)
                .failedAttempts(0)
                .build();

        return userRepository.save(user);  // Saves to MySQL
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean verifyPassword(String raw, String hashed) {
        return passwordEncoder.matches(raw, hashed);  // Compares plain vs hash
    }

    private static final long LOCKOUT_DURATION_MINUTES = 15;

    public User authenticate(String username, String password) {
        User user;
        try {
            user = findByUsername(username);
        } catch (RuntimeException e) {
            // Throw generic message to prevent user enumeration
            throw new RuntimeException("Invalid credentials");
        }

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            if (unlockWhenTimeExpired(user)) {
                // Account unlocked successfully
            } else {
                long minutesLeft = LOCKOUT_DURATION_MINUTES - java.time.Duration.between(user.getLockTime(), java.time.LocalDateTime.now()).toMinutes();
                throw new RuntimeException("Account is locked due to 5 consecutive failed login attempts. Try again in " + Math.max(1, minutesLeft) + " minutes.");
            }
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            increaseFailedAttempts(user);
            int remaining = 5 - user.getFailedAttempts();
            if (remaining <= 0) {
                throw new RuntimeException("Account locked due to 5 failed login attempts. Try again in 15 minutes.");
            } else {
                throw new RuntimeException("Invalid credentials. " + remaining + " attempts remaining.");
            }
        }

        // Credentials correct, reset attempts
        resetFailedAttempts(user);
        return user;
    }

    private void increaseFailedAttempts(User user) {
        int newFailAttempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(newFailAttempts);
        if (newFailAttempts >= 5) {
            user.setAccountNonLocked(false);
            user.setLockTime(java.time.LocalDateTime.now());
        }
        userRepository.save(user);
    }

    private void resetFailedAttempts(User user) {
        if (user.getFailedAttempts() > 0) {
            user.setFailedAttempts(0);
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            userRepository.save(user);
        }
    }

    private boolean unlockWhenTimeExpired(User user) {
        if (user.getLockTime() == null) {
            return true;
        }
        java.time.LocalDateTime lockTime = user.getLockTime();
        java.time.LocalDateTime currentTime = java.time.LocalDateTime.now();
        if (lockTime.plusMinutes(LOCKOUT_DURATION_MINUTES).isBefore(currentTime)) {
            user.setAccountNonLocked(true);
            user.setFailedAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
