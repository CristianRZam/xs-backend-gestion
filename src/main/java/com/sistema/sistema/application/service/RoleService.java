package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.Role.RoleCreateRequest;
import com.sistema.sistema.application.dto.request.Role.RoleUpdateRequest;
import com.sistema.sistema.application.dto.request.Role.RoleViewRequest;
import com.sistema.sistema.application.dto.response.RoleViewResponse;
import com.sistema.sistema.application.dto.response.role.RoleDto;
import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.repository.PermissionRepository;
import com.sistema.sistema.domain.repository.RoleRepository;
import com.sistema.sistema.domain.usecase.RoleUseCase;
import com.sistema.sistema.infrastructure.persistence.role.RoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService implements RoleUseCase {
    private final RoleRepository repository;
    private final PermissionRepository repositoryPermission;
    private final RoleMapper mapper;

    public RoleService(RoleRepository roleRepository, PermissionRepository repositoryPermission, RoleMapper mapper)
    {
        this.repository = roleRepository;
        this.mapper = mapper;
        this.repositoryPermission = repositoryPermission;
    }

    @Override
    public RoleViewResponse init(RoleViewRequest request) {
        // Los roles no eliminados
        List<Role> roles = repository.findByDeletedAtIsNull(request);

        // Mapear a DTO
        List<RoleDto> roleDtos = roles.stream()
                .map(mapper::toDto)
                .toList();
        // Total de roles
        Long total = (long) roles.size();

        // Contamos roles activos
        Long activeRoles = roles.stream()
                .filter(Role::getActive)
                .count();

        // Roles inactivos
        Long inactiveRoles = total - activeRoles;

        // Total de permisos
        Long totalPermissions = (long) repositoryPermission.findByDeletedAtIsNull().size();

        return RoleViewResponse.builder()
                .roles(roleDtos)
                .totalRoles(total)
                .activeRoles(activeRoles)
                .inactiveRoles(inactiveRoles)
                .totalPermissions(totalPermissions)
                .build();
    }



    @Override
    public List<Role> listRoles() {
        return repository.findAll();
    }

    @Override
    public Role getRoleById(Long id) {
        return repository.getRoleById(id);
    }

    @Override
    public Role create(RoleCreateRequest request) {
        Role role = mapper.toDomain(request);
        return repository.save(role);
    }

    @Override
    public Role update(RoleUpdateRequest request) {
        Role role = mapper.toDomain(request);
        return repository.update(role);
    }

    @Override
    public Boolean delete(Long id) {
        return repository.delete(id);
    }

    @Override
    public Boolean updateStatus(Long id) {
        return repository.updateStatus(id);
    }
}
