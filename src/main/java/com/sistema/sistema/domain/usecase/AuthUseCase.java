package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.LoginRequest;
import com.sistema.sistema.application.dto.response.auth.LoginResponse;
import jakarta.validation.Valid;


public interface AuthUseCase {

    LoginResponse login(@Valid LoginRequest request);
}