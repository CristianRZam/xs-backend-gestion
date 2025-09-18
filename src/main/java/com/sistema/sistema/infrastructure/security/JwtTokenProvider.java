package com.sistema.sistema.infrastructure.security;

import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Genera un JWT. - Se aseguran iat y exp en segundos (UTC) en el payload (claims)
     * - También se setean setIssuedAt / setExpiration (fechas en milisegundos) para JJWT.
     */
    public String generateToken(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        // Hora actual en UTC
        Instant nowUtc = Instant.now();
        Instant expInstant = nowUtc.plusMillis(jwtExpirationMs);

        long nowSeconds = nowUtc.getEpochSecond();      // segundos epoch UTC
        long expSeconds = expInstant.getEpochSecond();  // segundos epoch UTC

        Date issuedAt = Date.from(nowUtc);              // Date (ms) para JJWT
        Date expiry = Date.from(expInstant);            // Date (ms) para JJWT

        // Claims custom
        Claims claims = Jwts.claims().setSubject(user.getEmail());
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("username", user.getUsername());
        claims.put("active", user.getActive());
        // Asegurar tipos serializables (listas)
        List<String> roles = user.getRoles() != null
                ? user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList())
                : new ArrayList<>();
        claims.put("roles", roles);

        List<String> permissions = user.getPermissions() != null
                ? new ArrayList<>(new HashSet<>(user.getPermissions()))
                : new ArrayList<>();
        claims.put("roles", roles);
        claims.put("permissions", permissions);

        // Añadimos explícitamente iat y exp en segundos (compatibilidad con librerías cliente)
        claims.put("iat", nowSeconds);
        claims.put("exp", expSeconds);

        // Llave secreta segura
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        // Construir token: setClaims + setIssuedAt + setExpiration
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
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
        } catch (Exception e) {
            return false;
        }
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}
