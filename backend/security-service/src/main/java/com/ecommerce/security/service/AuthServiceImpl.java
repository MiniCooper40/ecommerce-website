package com.ecommerce.security.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.security.dto.AuthResponse;
import com.ecommerce.security.dto.LoginRequest;
import com.ecommerce.security.dto.RegisterRequest;
import com.ecommerce.security.entity.Role;
import com.ecommerce.security.entity.Role.RoleName;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.exception.AuthenticationException;
import com.ecommerce.security.exception.InvalidTokenException;
import com.ecommerce.security.exception.UserAlreadyExistsException;
import com.ecommerce.security.exception.UserNotFoundException;
import com.ecommerce.security.exception.ValidationException;
import com.ecommerce.security.repository.RoleRepository;
import com.ecommerce.security.repository.UserRepository;
import com.ecommerce.security.util.JwtUtil;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());

        // Set default roles
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new ValidationException("User Role not found."));
        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new ValidationException("Customer Role not found."));
        
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

    @Override
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmailWithRoles(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Check if user is enabled
        if (!user.getEnabled()) {
            throw new AuthenticationException("User account is disabled");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        // Create response
        return new AuthResponse(token, jwtUtil.getExpirationTime(), convertToUserDto(user));
    }

    @Override
    public AuthResponse refresh(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ValidationException("Invalid authorization header");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            throw new InvalidTokenException("Invalid or expired token");
        }

        // Refresh the token
        String newToken = jwtUtil.refreshToken(token);
        String userId = jwtUtil.getUserIdFromToken(newToken);

        // Get user details
        User user = userRepository.findByIdWithRoles(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new AuthResponse(newToken, jwtUtil.getExpirationTime(), convertToUserDto(user));
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