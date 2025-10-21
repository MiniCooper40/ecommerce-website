package com.ecommerce.catalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * JWT Security Configuration for Catalog Service.
 * 
 * This configuration:
 * - Validates JWTs using the security-service JWK endpoint
 * - Uses Spring Security's default JWT converter (handles SCOPE_ prefixed authorities)
 * - Provides stateless authentication
 * - Protects all endpoints except health/actuator endpoints
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class JwtSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - allow anyone to view products
                .requestMatchers("/actuator/**", "/health/**", "/info/**").permitAll()
                .requestMatchers("/api/products/**").permitAll() // Allow public product browsing
                // Admin endpoints require ADMIN authority (using SCOPE_ prefix)
                .requestMatchers("/api/products/admin/**").hasAuthority("SCOPE_ADMIN")
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );

        return http.build();
    }
}