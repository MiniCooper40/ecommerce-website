package com.ecommerce.security.service;

import com.ecommerce.security.dto.AuthResponse;
import com.ecommerce.security.dto.LoginRequest;
import com.ecommerce.security.dto.RegisterRequest;
import com.ecommerce.security.entity.Role;
import com.ecommerce.security.entity.Role.RoleName;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.RoleRepository;
import com.ecommerce.security.repository.UserRepository;
import com.ecommerce.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEnabled(true);

        // Set default roles
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new RuntimeException("User Role not found."));
        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Customer Role not found."));
        
        roles.add(userRole);
        roles.add(customerRole);
        user.setRoles(roles);

        // Save user
        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser);

        // Create response
        return new AuthResponse(token, jwtUtil.getExpirationTime(), convertToUserDto(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmailWithRoles(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Check if user is enabled
        if (!user.getEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        // Create response
        return new AuthResponse(token, jwtUtil.getExpirationTime(), convertToUserDto(user));
    }

    public AuthResponse refresh(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }

        // Refresh the token
        String newToken = jwtUtil.refreshToken(token);
        String userId = jwtUtil.getUserIdFromToken(newToken);

        // Get user details
        User user = userRepository.findByIdWithRoles(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(newToken, jwtUtil.getExpirationTime(), convertToUserDto(user));
    }

    public Boolean validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);
        return jwtUtil.validateToken(token);
    }

    private AuthResponse.UserDto convertToUserDto(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        AuthResponse.UserDto userDto = new AuthResponse.UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setRoles(roleNames);
        userDto.setCreatedAt(user.getCreatedAt());

        return userDto;
    }
}