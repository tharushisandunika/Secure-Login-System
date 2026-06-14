package com.example.securelogin.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(". "));
        
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Validation failed: " + errors
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericExceptions(Exception ex) {
        // Return a generic error message to prevent leaking internal stack trace details
        return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "An unexpected error occurred. Please try again later."
        ));
    }
}
