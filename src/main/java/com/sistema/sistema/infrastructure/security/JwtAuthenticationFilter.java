package com.sistema.sistema.infrastructure.security;

import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * NO filtrar:
     * - OPTIONS (CORS)
     * - archivos estáticos (Angular)
     * - rutas públicas
     * SOLO filtrar /api/**
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();

        // Permitir Angular completamente
        if (path.equals("/") ||
                path.startsWith("/index") ||
                path.startsWith("/static/") ||
                path.startsWith("/assets/") ||
                path.endsWith(".js") ||
                path.endsWith(".css") ||
                path.endsWith(".json") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".ico")) {
            return true; // NO filtrar
        }

        // Permitir login
        if (path.equals("/api/login")) {
            return true;
        }

        // Permitir OPTIONS para CORS
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            return true;
        }

        // Solo filtrar /api/**
        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (token != null && tokenProvider.validateToken(token)) {

            String username = tokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!userDetails.isEnabled()) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write(
                        Objects.requireNonNull(ApiResponseFactory.error(
                                HttpStatus.UNAUTHORIZED, "El usuario está deshabilitado", List.of()
                        ).getBody()).toString()
                );
                return;
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
