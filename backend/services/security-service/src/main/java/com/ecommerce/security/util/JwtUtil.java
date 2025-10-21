package com.ecommerce.security.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ecommerce.security.entity.Role;
import com.ecommerce.security.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private String keyId;

    @PostConstruct
    public void init() {
        try {
            // Generate RSA key pair for JWT signing
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(2048);
            KeyPair keyPair = keyGenerator.generateKeyPair();
            
            this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
            this.publicKey = (RSAPublicKey) keyPair.getPublic();
            this.keyId = UUID.randomUUID().toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }

    private PrivateKey getSigningKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getKeyId() {
        return keyId;
    }

    public String generateToken(User user) {
        // Add small random component to ensure unique timestamps even in rapid succession
        long nowTime = System.currentTimeMillis() + (System.nanoTime() % 1000) + (int)(Math.random() * 1000);
        Date now = new Date(nowTime);
        Date expiryDate = new Date(nowTime + jwtExpiration);

        // Extract raw role names for Spring's standard scope claim
        List<String> scopes = user.getRoles().stream()
                .map(Role::toString)
                .collect(Collectors.toList());

        // Generate unique JWT ID to ensure token uniqueness
        String jwtId = java.util.UUID.randomUUID().toString();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("scope", String.join(" ", scopes))  // Space-separated as per OAuth2/OpenID standards
                .id(jwtId)  // Add unique JWT ID
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("email", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("roles", List.class);
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Boolean validateToken(String token, User user) {
        final String userId = getUserIdFromToken(token);
        return (userId.equals(user.getId().toString()) && !isTokenExpired(token));
    }

    public String refreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Ensure timestamp uniqueness by guaranteeing a different iat
            Date originalIat = claims.getIssuedAt();
            Date now = new Date();
            
            // Always ensure new iat is at least 1 second after original, or current time if later
            long newIatTime = Math.max(now.getTime(), 
                (originalIat != null ? originalIat.getTime() + 1000 : now.getTime()));
            
            // Add small random component to prevent any potential collisions
            newIatTime += (System.nanoTime() % 1000);
            
            Date newIat = new Date(newIatTime);
            Date expiryDate = new Date(newIatTime + jwtExpiration);

            // Generate new unique JWT ID for refreshed token
            String newJwtId = java.util.UUID.randomUUID().toString();

            // Create a new token with guaranteed unique timestamp and ID
            return Jwts.builder()
                    .subject(claims.getSubject())
                    .claim("email", claims.get("email"))
                    .claim("firstName", claims.get("firstName"))
                    .claim("lastName", claims.get("lastName"))
                    .claim("scope", claims.get("scope"))  // Preserve scope claim
                    .claim("roles", claims.get("roles"))  // Keep legacy roles for backward compatibility
                    .id(newJwtId)  // Add unique JWT ID
                    .issuedAt(newIat)
                    .expiration(expiryDate)
                    .signWith(getSigningKey())
                    .compact();
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid token for refresh", e);
        }
    }

    public Long getExpirationTime() {
        return jwtExpiration;
    }
}