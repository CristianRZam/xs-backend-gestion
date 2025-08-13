package com.sistema.sistema.infrastructure.persistence.user;

import com.sistema.sistema.domain.model.User;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByUsername(String username);
    User save(User user);
}
