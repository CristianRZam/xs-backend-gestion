package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.Permission.PermissionUpdateRequest;
import com.sistema.sistema.application.dto.response.permission.PermissionViewResponse;
import com.sistema.sistema.domain.model.Permission;
import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.repository.PermissionRepository;
import com.sistema.sistema.domain.repository.RolePermissionRepository;
import com.sistema.sistema.domain.repository.RoleRepository;
import com.sistema.sistema.domain.usecase.PermissionUseCase;
import com.sistema.sistema.infrastructure.persistence.role.RoleMapper;
import com.sistema.sistema.infrastructure.persistence.rolepermission.RolePermissionEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PermissionService implements PermissionUseCase {
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository repository;

    public PermissionService(PermissionRepository repository, RoleRepository roleRepository, RolePermissionRepository rolePermissionRepository, RoleMapper mapper) {
        this.roleRepository= roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.repository = repository;
    }

    @Override
    public PermissionViewResponse getByRoleId(Long id) {
        Role role = roleRepository.getRoleById(id);
        List<Permission> allPermissions = repository.findByDeletedAtIsNull();
        List<Permission> assignedPermissions = repository.findByRoleId(id);

        return PermissionViewResponse.builder()
                .role(role)
                .allPermissions(allPermissions)
                .assignedPermissions(assignedPermissions)
                .build();
    }

    @Override
    public Boolean updatePermissionByRole(PermissionUpdateRequest request) {
        return rolePermissionRepository.updatePermissionByRole(request);
    }


}
