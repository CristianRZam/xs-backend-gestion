package com.sistema.sistema.infrastructure.security;

import com.sistema.sistema.domain.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public String generateToken(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        Claims claims = Jwts.claims().setSubject(user.getUsername());

        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("username", user.getUsername());
        claims.put("isActive", user.getIsActive());
        claims.put("roles", user.getRoles() != null ? new ArrayList<>(user.getRoles()) : new ArrayList<>());
        claims.put("permissions", user.getPermissions() != null ? new ArrayList<>(user.getPermissions()) : new ArrayList<>());

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        // Convertimos el jwtSecret en SecretKey seguro
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}
