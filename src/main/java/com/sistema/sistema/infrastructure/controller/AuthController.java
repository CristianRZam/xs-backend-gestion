package com.sistema.sistema.infrastructure.controller;


import com.sistema.sistema.application.dto.request.LoginRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.auth.LoginResponse;
import com.sistema.sistema.domain.usecase.AuthUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse result = authUseCase.login(request);
        return ApiResponseFactory.success(result, "Inicio de sesión exitoso. Token generado correctamente.");
    }



}
