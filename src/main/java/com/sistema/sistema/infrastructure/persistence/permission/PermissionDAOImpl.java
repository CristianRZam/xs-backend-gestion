package com.sistema.sistema.infrastructure.persistence.permission;

import com.sistema.sistema.domain.model.Permission;
import com.sistema.sistema.domain.repository.PermissionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PermissionDAOImpl implements PermissionRepository {
    private final PermissionMapper mapper;
    private final JpaPermissionRepository jpa;
    PermissionDAOImpl(PermissionMapper mapper, JpaPermissionRepository jpa) {
        this.mapper = mapper;
        this.jpa = jpa;
    }

    @Override
    public List<Permission> findByDeletedAtIsNull() {
        return jpa.findByDeletedAtIsNullOrderByIdAsc()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Permission> findByRoleId(long id) {
        return jpa.findByRoleId(id)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

}
