package com.sistema.sistema.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.sistema.application.dto.response.AppVersionResponse;
import com.sistema.sistema.application.service.AppVersionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppVersionFilter extends OncePerRequestFilter {
    private final AppVersionService service;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String platform = request.getHeader("X-App-Platform");
        return !path.startsWith("/api/")
                || path.equals("/api/app-version/check")
                || platform == null
                || platform.isBlank();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        AppVersionResponse version = service.check(
                request.getHeader("X-App-Platform"), request.getHeader("X-App-Build"));
        if (!version.isUpdateRequired()) {
            chain.doFilter(request, response);
            return;
        }
        log.warn("Blocked request from unsupported application version on {}", request.getRequestURI());
        response.setStatus(426);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "status", 426,
                "success", false,
                "message", version.getMessage(),
                "data", version,
                "errors", java.util.List.of("APP_UPDATE_REQUIRED")
        ));
    }
}
