package com.sistema.sistema.domain.repository;

import com.sistema.sistema.application.dto.request.Permission.PermissionUpdateRequest;

public interface RolePermissionRepository {
    Boolean updatePermissionByRole(PermissionUpdateRequest request);
}
