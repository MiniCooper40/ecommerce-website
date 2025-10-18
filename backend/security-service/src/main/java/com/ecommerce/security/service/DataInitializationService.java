package com.ecommerce.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ecommerce.security.entity.Role;
import com.ecommerce.security.entity.Role.RoleName;
import com.ecommerce.security.repository.RoleRepository;

@Component
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
    }

    private void initializeRoles() {
        // Create default roles if they don't exist
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role(roleName, roleName.getDescription());
                roleRepository.save(role);
                System.out.println("Created role: " + roleName.name());
            }
        }
    }
}