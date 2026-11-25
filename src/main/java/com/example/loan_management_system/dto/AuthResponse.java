package com.example.loan_management_system.dto;

public class AuthResponse {

    private String token;
    private Long userId;
    private String email;
    private String role;
    private String message;

    public AuthResponse(String token, Long userId, String email, String role, String message) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.message = message;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getMessage() { return message; }
}