package com.sistema.sistema.infrastructure.controller;


import com.sistema.sistema.application.dto.request.LoginRequest;
import com.sistema.sistema.domain.service.AuthUseCase;
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Map<String, Object> result = authUseCase.login(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(result);
    }


}
