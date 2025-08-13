package com.sistema.sistema.domain.service;

import java.util.Map;

public interface AuthUseCase {
    Map<String, Object> login(String username, String password);
}