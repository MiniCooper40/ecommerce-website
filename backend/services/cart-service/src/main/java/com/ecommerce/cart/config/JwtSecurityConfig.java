package com.ecommerce.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

/**
 * JWT Security Configuration for Cart Service.
 * 
 * This configuration:
 * - Validates JWTs using the security-service JWK endpoint
 * - Uses Spring Security's default JWT converter (handles SCOPE_ prefixed authorities)
 * - Provides stateless authentication
 * - Protects cart endpoints requiring user authentication
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class JwtSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);
        
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers(
                    mvcMatcherBuilder.pattern("/actuator/**"),
                    mvcMatcherBuilder.pattern("/health/**"),
                    mvcMatcherBuilder.pattern("/info/**")
                ).permitAll()
                // Admin endpoints require ADMIN authority (using SCOPE_ prefix)
                .requestMatchers(mvcMatcherBuilder.pattern("/cart/admin/**")).hasAuthority("SCOPE_ADMIN")
                // Cart operations require authentication (users can only access their own carts)
                .requestMatchers(mvcMatcherBuilder.pattern("/cart/**")).authenticated()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );

        return http.build();
    }
}