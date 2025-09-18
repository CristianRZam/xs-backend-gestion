package com.sistema.sistema.infrastructure.persistence.role;

import com.sistema.sistema.application.dto.request.Role.RoleUpdateRequest;
import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.infrastructure.persistence.permission.PermissionEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.sistema.sistema.application.dto.request.Role.RoleCreateRequest;

@Component
public class RoleMapper {

    // DTO → Domain
    public Role toDomain(RoleCreateRequest request) {
        if (request == null) return null;

        return Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();
    }

    public Role toDomain(RoleUpdateRequest request) {
        if (request == null) return null;

        return Role.builder()
                .id(request.getId())
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    // Entity → Domain
    public Role toDomain(RoleEntity e) {
        if (e == null) return null;

        return Role.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .active(e.getActive())

                // Auditoría
                .createdBy(e.getCreatedBy())
                .createdAt(e.getCreatedAt())
                .modifiedBy(e.getModifiedBy())
                .modifiedAt(e.getModifiedAt())
                .deletedBy(e.getDeletedBy())
                .deletedAt(e.getDeletedAt())

                // Permisos
                .permissions(
                        e.getPermissions()
                                .stream()
                                .map(PermissionEntity::getName)
                                .collect(Collectors.toSet())
                )
                .build();
    }

    // Domain → Entity
    public RoleEntity toEntity(Role d) {
        if (d == null) return null;

        Set<PermissionEntity> permissions = new HashSet<>();
        if (d.getPermissions() != null) {
            permissions = d.getPermissions()
                    .stream()
                    .map(name -> PermissionEntity.builder().name(name).build())
                    .collect(Collectors.toSet());
        }

        return RoleEntity.builder()
                .id(d.getId())
                .name(d.getName())
                .description(d.getDescription())
                .active(d.getActive() != null ? d.getActive() : true)

                // Auditoría
                .createdBy(d.getCreatedBy())
                .createdAt(d.getCreatedAt())
                .modifiedBy(d.getModifiedBy())
                .modifiedAt(d.getModifiedAt())
                .deletedBy(d.getDeletedBy())
                .deletedAt(d.getDeletedAt())

                .permissions(permissions)
                .build();
    }
}
