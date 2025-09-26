package com.sistema.sistema.infrastructure.security;

import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
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

        Instant nowUtc = Instant.now();
        Instant expInstant = nowUtc.plusMillis(jwtExpirationMs);

        long nowSeconds = nowUtc.getEpochSecond();
        long expSeconds = expInstant.getEpochSecond();

        Date issuedAt = Date.from(nowUtc);
        Date expiry = Date.from(expInstant);

        Claims claims = Jwts.claims().setSubject(user.getEmail());
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("username", user.getUsername());
        claims.put("fullname", user.getPerson() != null ? user.getPerson().getFullName() : null);
        claims.put("active", user.getActive());

        List<String> roles = new ArrayList<>();
        Set<String> permissions = new HashSet<>();

        if (user.getRoles() != null) {
            user.getRoles().forEach(userRole -> {
                // 🚨 validar que el UserRole no esté eliminado
                if (userRole == null || Boolean.TRUE.equals(userRole.getDeleted())) return;

                Role role = userRole.getRole();
                if (role != null && Boolean.TRUE.equals(role.getActive())) {
                    roles.add(role.getName());

                    if (role.getPermissions() != null) {
                        role.getPermissions().forEach(permission -> {
                            if (permission != null && permission.getName() != null) {
                                permissions.add(permission.getName());
                            }
                        });
                    }
                }
            });
        }

        if (roles.isEmpty()) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "El usuario no tiene roles activos y no puede acceder al sistema.");
        }

        if (permissions.isEmpty()) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Su usuario no tiene permisos asignados para acceder al sistema.");
        }

        claims.put("roles", roles);
        claims.put("permissions", new ArrayList<>(permissions));
        claims.put("iat", nowSeconds);
        claims.put("exp", expSeconds);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

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
