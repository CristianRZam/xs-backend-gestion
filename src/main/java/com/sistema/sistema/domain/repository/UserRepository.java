package com.sistema.sistema.domain.repository;

import com.sistema.sistema.application.dto.request.user.UserViewRequest;
import com.sistema.sistema.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    User save(User user);
    User update(User existingUser);
    List<User> ByDeletedAtIsNull(UserViewRequest request);
    User getUserById(Long id);
    Boolean delete(Long id);
    Boolean updateStatus(Long id);
}