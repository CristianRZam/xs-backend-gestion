package com.sistema.sistema.infrastructure.persistence.rolepermission;

import com.sistema.sistema.application.dto.request.Permission.PermissionUpdateRequest;
import com.sistema.sistema.domain.repository.RolePermissionRepository;
import com.sistema.sistema.infrastructure.persistence.permission.PermissionEntity;
import com.sistema.sistema.infrastructure.persistence.permission.JpaPermissionRepository;
import com.sistema.sistema.infrastructure.persistence.role.RoleEntity;
import com.sistema.sistema.infrastructure.persistence.role.JpaRoleRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class RolePermissionDAOImpl implements RolePermissionRepository {
    private final JpaRolePermissionRepository jpa;
    private final JpaRoleRepository roleJpa;
    private final JpaPermissionRepository permissionJpa;

    public RolePermissionDAOImpl(JpaRolePermissionRepository jpa,
                                 JpaRoleRepository roleJpa,
                                 JpaPermissionRepository permissionJpa) {
        this.jpa = jpa;
        this.roleJpa = roleJpa;
        this.permissionJpa = permissionJpa;
    }

    @Override
    public Boolean updatePermissionByRole(PermissionUpdateRequest request) {
        Long roleId = request.getRoleId();
        List<Long> newPermissionIds = request.getPermissionIds();

        // Traemos todas las asignaciones actuales (incluyendo eliminadas)
        List<RolePermissionEntity> currentAssignments = jpa.findRolePermissionsByRoleId(roleId);

        // Map actual para búsqueda rápida
        Map<Long, RolePermissionEntity> currentMap = currentAssignments.stream()
                .collect(Collectors.toMap(rp -> rp.getPermission().getId(), rp -> rp));

        // Eliminamos lógicamente los permisos que ya no están
        for (RolePermissionEntity rp : currentAssignments) {
            if (!newPermissionIds.contains(rp.getPermission().getId()) && rp.getDeletedAt() == null) {
                rp.setModifiedBy(1L);
                rp.setDeletedAt(LocalDateTime.now());
                rp.setDeletedBy(1L); // aquí puedes usar usuario actual
            }
        }

        // Creamos o reactivamos permisos
        RoleEntity role = roleJpa.findById(roleId).orElseThrow();
        List<RolePermissionEntity> toSave = new ArrayList<>();

        for (Long permissionId : newPermissionIds) {
            RolePermissionEntity existing = currentMap.get(permissionId);

            if (existing == null) {
                // Crear nuevo
                PermissionEntity permission = permissionJpa.findById(permissionId).orElseThrow();
                RolePermissionEntity rp = RolePermissionEntity.builder()
                        .id(new RolePermissionKey(roleId, permissionId))
                        .role(role)
                        .permission(permission)
                        .assignedAt(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .createdBy(1L)
                        .build();
                toSave.add(rp);
            } else if (existing.getDeletedAt() != null) {
                // Reactivar
                existing.setDeletedAt(null);
                existing.setDeletedBy(null);
                existing.setModifiedAt(LocalDateTime.now());
                existing.setModifiedBy(1L);
                toSave.add(existing);
            }
        }

        jpa.saveAll(toSave);
        return true;
    }
}
