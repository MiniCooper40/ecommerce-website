package com.ecommerce.security.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ecommerce.security.entity.Role;
import com.ecommerce.security.entity.Role.RoleName;
import com.ecommerce.security.entity.User;
import com.ecommerce.security.repository.RoleRepository;
import com.ecommerce.security.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminUser();
    }

    private void initializeRoles() {
        // Create default roles if they don't exist
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                role.setDescription(roleName.getDescription());
                roleRepository.save(role);
                log.info("Created role: {}", roleName.name());
            }
        }
    }

    private void initializeAdminUser() {
        // Check if admin user already exists
        String adminEmail = "admin@ecommerce.com";
        
        if (!userRepository.existsByEmail(adminEmail)) {
            // Get admin role
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            
            Role userRole = roleRepository.findByName(RoleName.USER)
                    .orElseThrow(() -> new RuntimeException("User role not found"));
            
            // Create admin user
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);
            
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .phoneNumber("+1-555-0100")
                    .enabled(true)
                    .roles(roles)
                    .build();
            
            userRepository.save(admin);
            log.info("=====================================================");
            log.info("Created admin user:");
            log.info("  Email: {}", adminEmail);
            log.info("  Password: admin123");
            log.info("  Roles: ADMIN, USER");
            log.info("=====================================================");
        } else {
            log.info("Admin user already exists");
        }
    }
}