package com.sistema.sistema.domain.repository;

import com.sistema.sistema.domain.model.Permission;

import java.util.List;

public interface PermissionRepository {
    List<Permission> findByDeletedAtIsNull();
    List<Permission> findByRoleId(long id);
}
