package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.Permission.PermissionUpdateRequest;
import com.sistema.sistema.application.dto.response.permission.PermissionViewResponse;

public interface PermissionUseCase {
    PermissionViewResponse getByRoleId(Long id);
    Boolean updatePermissionByRole(PermissionUpdateRequest request);
}
