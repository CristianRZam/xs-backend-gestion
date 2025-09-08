package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.Role.RoleCreateRequest;
import com.sistema.sistema.application.dto.request.Role.RoleUpdateRequest;
import com.sistema.sistema.application.dto.request.Role.RoleViewRequest;
import com.sistema.sistema.application.dto.response.RoleViewResponse;
import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.repository.RoleRepository;
import com.sistema.sistema.domain.service.RoleUseCase;
import com.sistema.sistema.infrastructure.persistence.role.RoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService implements RoleUseCase {
    private final RoleRepository repository;
    private final RoleMapper mapper;

    public RoleService(RoleRepository roleRepository, RoleMapper mapper)
    {
        this.repository = roleRepository;
        this.mapper = mapper;
    }

    @Override
    public RoleViewResponse init(RoleViewRequest request) {
        // Los roles no eliminados
        List<Role> roles = repository.findByDeletedAtIsNull(request);

        // Total de roles
        Long total = (long) roles.size();

        // Contamos roles activos
        Long activeRoles = roles.stream()
                .filter(Role::getActive)
                .count();

        // Roles inactivos
        Long inactiveRoles = total - activeRoles;

        // Total de permisos
        Long totalPermissions = roles.stream()
                .mapToLong(r -> r.getPermissions() != null ? r.getPermissions().size() : 0)
                .sum();

        return RoleViewResponse.builder()
                .roles(roles)
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
        return repository.save(role);
    }

    @Override
    public Boolean delete(Long id) {
        return repository.delete(id);
    }
}
