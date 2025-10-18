package com.ecommerce.security.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/.well-known")
public class JwksController {

    @Value("${jwt.secret:mySecretKey}")
    private String jwtSecret;

    @GetMapping("/jwks.json")
    public Map<String, Object> getJwks() {
        // For demo purposes - in production, use RSA keys
        // This is a simplified JWKS for HMAC signatures
        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", new Object[]{createJwk()});
        return jwks;
    }

    private Map<String, Object> createJwk() {
        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kty", "oct"); // Key type: octet sequence (for HMAC)
        jwk.put("use", "sig"); // Use: signature
        jwk.put("alg", "HS256"); // Algorithm
        jwk.put("k", Base64.getUrlEncoder().withoutPadding()
            .encodeToString(jwtSecret.getBytes(StandardCharsets.UTF_8))); // Key value
        return jwk;
    }
}