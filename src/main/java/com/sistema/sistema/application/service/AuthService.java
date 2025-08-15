package com.sistema.sistema.application.service;

import com.sistema.sistema.domain.exceptions.ClientException;
import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.domain.repository.UserRepository;
import com.sistema.sistema.domain.service.AuthUseCase;
import com.sistema.sistema.infrastructure.security.JwtTokenProvider;
import com.sistema.sistema.shared.constants.ErrorCodes;
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
    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ClientException(ErrorCodes.INVALID_CREDENTIALS, "Credenciales inválidas"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ClientException(ErrorCodes.INVALID_CREDENTIALS, "Credenciales inválidas");
        }

        String token = jwtTokenProvider.generateToken(user);
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("expiresIn", jwtTokenProvider.getExpirationMs());
        return resp;
    }

}
