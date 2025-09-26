package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.LoginRequest;
import com.sistema.sistema.application.dto.response.auth.LoginResponse;
import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.domain.repository.UserRepository;
import com.sistema.sistema.domain.usecase.AuthUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import com.sistema.sistema.infrastructure.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


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
    public LoginResponse login(LoginRequest request) {
        // 1. Buscar usuario por email o username
        User user = userRepository.findByEmail(request.getEmail())
                .or(() -> userRepository.findByUsername(request.getEmail()))
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED,
                        "Credenciales inválidas"));

        // 2. Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas");
        }

        // 3. Verificar si el usuario fue eliminado lógicamente
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "El acceso ha sido denegado porque su cuenta ha sido eliminada. " +
                            "Por favor, contacte al administrador para más información.");
        }

        // 4. Verificar si el usuario está desactivado
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "El acceso ha sido denegado porque su cuenta está desactivada. " +
                            "Por favor, contacte al administrador para reactivar su cuenta.");
        }

        // 5. Verificar que tenga al menos un rol y que no sea solo cliente
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "El usuario no tiene roles asignados y no puede acceder al sistema. " +
                            "Por favor, contacte al administrador.");
        }

        // 6. Generar token
        String token = jwtTokenProvider.generateToken(user);

        return new LoginResponse(token);
    }



}
