package com.ecommerce.shared.testutil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockUserPrincipalSecurityContextFactory
        implements WithSecurityContextFactory<WithMockUserPrincipal> {

    @Override
    public SecurityContext createSecurityContext(WithMockUserPrincipal annotation) {
        // Map roles -> authorities using SCOPE_ prefix (Spring JWT default)
        List<GrantedAuthority> authorities = Arrays.stream(annotation.roles())
                .map(role -> new SimpleGrantedAuthority("SCOPE_" + role))
                .collect(Collectors.toList());

        // Create space-separated scope string for the scope claim (Spring JWT standard)
        String scopeClaim = String.join(" ", annotation.roles());

        // Build a fake JWT (token value doesn't matter)
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", annotation.userId())
                .claim("scope", scopeClaim)  // Spring's default claim for authorities
                .build();

        // Create a JwtAuthenticationToken so Spring Security treats it like a real JWT-auth user
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);

        // Set it in the SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}