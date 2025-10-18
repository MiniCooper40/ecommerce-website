package com.ecommerce.security.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long expiresIn;
    private UserDto user;
    private LocalDateTime issuedAt;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String token, Long expiresIn, UserDto user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
        this.issuedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    // Inner class for user information
    public static class UserDto {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private List<String> roles;
        private LocalDateTime createdAt;

        // Constructors
        public UserDto() {}

        public UserDto(Long id, String email, String firstName, String lastName, List<String> roles) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.roles = roles;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}