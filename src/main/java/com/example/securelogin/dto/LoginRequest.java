// LoginRequest.java
package com.example.securelogin.dto;
import lombok.Data;

@Data  // Lombok: auto-creates getters and setters
public class LoginRequest {
    private String username;
    private String password;
}
