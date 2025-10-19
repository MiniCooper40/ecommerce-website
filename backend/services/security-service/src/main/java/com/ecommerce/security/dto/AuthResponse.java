package com.ecommerce.security.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long expiresIn;
    private UserDto user;
    private LocalDateTime issuedAt;

    // Custom constructor
    public AuthResponse(String token, Long expiresIn, UserDto user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
        this.issuedAt = LocalDateTime.now();
        this.type = "Bearer";
    }

    // Inner class for user information - using Lombok
    @Data
    @NoArgsConstructor
    public static class UserDto {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private List<String> roles;
        private LocalDateTime createdAt;

        public UserDto(Long id, String email, String firstName, String lastName, List<String> roles) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.roles = roles;
        }
    }
}