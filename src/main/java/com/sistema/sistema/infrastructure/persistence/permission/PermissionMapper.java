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
                .build();
    }
}
