package com.sistema.sistema.domain.repository;

import com.sistema.sistema.domain.model.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String username);
    User save(User user);
}