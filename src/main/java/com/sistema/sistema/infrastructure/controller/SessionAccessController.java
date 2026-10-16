package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Returns the permissions currently effective in the database for the active
 * session. JWT claims are intentionally not used as the source of truth here.
 */
@RestController
@RequestMapping("/api/session")
public class SessionAccessController {

    @GetMapping("/access")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> access(Authentication authentication) {
        List<String> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .sorted()
                .toList();

        return ApiResponseFactory.success(
                Map.of("permissions", permissions),
                "Permisos de sesion cargados correctamente"
        );
    }
}
