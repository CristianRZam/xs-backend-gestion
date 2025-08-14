package com.sistema.sistema.infrastructure.persistence.user;

import com.sistema.sistema.domain.model.User;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toDomain(UserEntity e) {
        if (e == null) return null;

        // Extraemos roles como nombres
        Set<String> roles = e.getRoles() != null
                ? e.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet())
                : Set.of();

        // Extraemos permisos de todos los roles
        Set<String> permissions = e.getRoles() != null
                ? e.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet())
                : Set.of();

        return User.builder()
                .id(e.getId())
                .username(e.getUsername())
                .email(e.getEmail())
                .password(e.getPassword())
                .roles(roles)
                .permissions(permissions)
                .isActive(e.getIsActive())
                .build();
    }

    public UserEntity toEntity(User d) {
        if (d == null) return null;

        // Persistir roles requiere cargar RoleEntity de DB. Aquí solo seteamos campos básicos
        return UserEntity.builder()
                .id(d.getId())
                .username(d.getUsername())
                .email(d.getEmail())
                .password(d.getPassword())
                .isActive(d.getIsActive())
                .build();
    }
}
