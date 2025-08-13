package com.sistema.sistema.infrastructure.persistence.user;

import com.sistema.sistema.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity e) {
        if (e == null) return null;
        return User.builder()
                .id(e.getId())
                .username(e.getUsername())
                .password(e.getPassword())
                .roles(e.getRoles())
                .build();
    }

    public UserEntity toEntity(User d) {
        if (d == null) return null;
        return UserEntity.builder()
                .id(d.getId())
                .username(d.getUsername())
                .password(d.getPassword())
                .roles(d.getRoles())
                .build();
    }
}
