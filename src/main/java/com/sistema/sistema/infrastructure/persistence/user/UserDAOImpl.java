package com.sistema.sistema.infrastructure.persistence.user;


import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDAOImpl implements UserRepository, UserDAO {

    private final JpaUserRepository jpa;
    private final UserMapper mapper;

    public UserDAOImpl(JpaUserRepository jpa, UserMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity e = mapper.toEntity(user);
        UserEntity saved = jpa.save(e);
        return mapper.toDomain(saved);
    }
}
