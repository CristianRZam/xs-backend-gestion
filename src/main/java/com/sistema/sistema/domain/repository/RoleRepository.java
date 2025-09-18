package com.sistema.sistema.domain.repository;

import com.sistema.sistema.application.dto.request.Role.RoleViewRequest;
import com.sistema.sistema.domain.model.Role;

import java.util.List;

public interface RoleRepository {
    List<Role> findAll();
    List<Role> findByDeletedAtIsNull(RoleViewRequest request);
    Role getRoleById(Long id);
    Role save(Role request);
    Boolean delete(Long id);
    Role update(Role request);
    Boolean updateStatus(Long id);
}
