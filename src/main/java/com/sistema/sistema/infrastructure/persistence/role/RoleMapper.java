package com.sistema.sistema.infrastructure.persistence.role;

import com.sistema.sistema.application.dto.request.Role.RoleUpdateRequest;
import com.sistema.sistema.application.dto.response.role.RoleDto;
import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.infrastructure.persistence.permission.PermissionEntity;
import com.sistema.sistema.infrastructure.persistence.permission.PermissionMapper;
import com.sistema.sistema.infrastructure.persistence.rolepermission.RolePermissionEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sistema.sistema.application.dto.request.Role.RoleCreateRequest;

@Component
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public RoleMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }
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

                // Permisos desde rolePermissions (solo activos)
                .permissions(
                        e.getRolePermissions() != null
                                ? e.getRolePermissions().stream()
                                .filter(rp -> rp.getDeletedAt() == null) // solo si no están eliminados
                                .map(RolePermissionEntity::getPermission)
                                .map(permissionMapper::toDomain)
                                .collect(Collectors.toList())
                                : List.of()
                )
                .build();
    }



    // Domain → Entity
    public RoleEntity toEntity(Role d) {
        if (d == null) return null;

        RoleEntity roleEntity = RoleEntity.builder()
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

                .build();

        // reconstruir RolePermissions desde las Permissions del dominio
        if (d.getPermissions() != null) {
            Set<RolePermissionEntity> rolePermissions = d.getPermissions().stream()
                    .map(permissionMapper::toEntity)
                    .map(p -> RolePermissionEntity.builder()
                            .role(roleEntity)
                            .permission(p)
                            .deletedAt(null) // al crear se asume activo
                            .build())
                    .collect(Collectors.toSet());
            roleEntity.setRolePermissions(rolePermissions);
        }

        return roleEntity;
    }


    public RoleDto toDto(Role role) {
        if (role == null) return null;

        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .deleted(role.getDeletedAt() != null)
                .build();
    }
}
