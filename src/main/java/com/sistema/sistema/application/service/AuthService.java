package com.sistema.sistema.application.service;

import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.domain.repository.UserRepository;
import com.sistema.sistema.domain.service.AuthUseCase;
import com.sistema.sistema.infrastructure.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService implements AuthUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("expiresIn", jwtTokenProvider.getExpirationMs());
        return resp;
    }
}
