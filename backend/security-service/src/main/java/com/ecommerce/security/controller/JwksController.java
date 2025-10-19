package com.ecommerce.security.controller;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.security.util.JwtUtil;

@RestController
@RequestMapping("/.well-known")
public class JwksController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/jwks.json")
    public Map<String, Object> getJwks() {
        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", new Object[]{createJwk()});
        return jwks;
    }

    private Map<String, Object> createJwk() {
        RSAPublicKey publicKey = (RSAPublicKey) jwtUtil.getPublicKey();
        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kty", "RSA"); // Key type: RSA
        jwk.put("use", "sig"); // Use: signature
        jwk.put("alg", "RS256"); // Algorithm
        jwk.put("kid", jwtUtil.getKeyId()); // Key ID
        
        // RSA public key components
        jwk.put("n", encodeBase64URL(publicKey.getModulus()));
        jwk.put("e", encodeBase64URL(publicKey.getPublicExponent()));
        
        return jwk;
    }

    private String encodeBase64URL(BigInteger value) {
        byte[] bytes = value.toByteArray();
        // Remove leading zero byte if present (for positive numbers)
        if (bytes[0] == 0 && bytes.length > 1) {
            byte[] newBytes = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, newBytes, 0, newBytes.length);
            bytes = newBytes;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}