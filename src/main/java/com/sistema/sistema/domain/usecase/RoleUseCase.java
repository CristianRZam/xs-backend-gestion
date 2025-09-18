package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.Role.RoleCreateRequest;
import com.sistema.sistema.application.dto.request.Role.RoleUpdateRequest;
import com.sistema.sistema.application.dto.request.Role.RoleViewRequest;
import com.sistema.sistema.application.dto.response.RoleViewResponse;
import com.sistema.sistema.domain.model.Role;

import java.util.List;

public interface RoleUseCase {
    RoleViewResponse init(RoleViewRequest request) ;
    List<Role> listRoles();
    Role getRoleById(Long id);
    Role create(RoleCreateRequest request);
    Role update(RoleUpdateRequest request);
    Boolean delete(Long id);
    Boolean updateStatus(Long id);
}
