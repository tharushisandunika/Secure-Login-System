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
}
