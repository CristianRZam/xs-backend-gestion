package com.sistema.sistema.infrastructure.persistence.permission;

import com.sistema.sistema.domain.model.Permission;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PermissionMapper {
    // Entity → Domain
    public Permission toDomain(PermissionEntity e) {
        if (e == null) return null;

        return Permission.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .module(e.getModule())
                .build();
    }

    // Domain → Entity
    public PermissionEntity toEntity(Permission d) {
        if (d == null) return null;

        return PermissionEntity.builder()
                .id(d.getId())
                .name(d.getName())
                .description(d.getDescription())
                .module(d.getModule())
                .build();
    }
}
