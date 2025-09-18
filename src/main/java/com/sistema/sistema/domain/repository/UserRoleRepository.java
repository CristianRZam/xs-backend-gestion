package com.sistema.sistema.domain.repository;

import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.model.User;

import java.util.Set;

public interface UserRoleRepository {
    void saveUserRoles(User user, Set<Role> roles, Long assignedBy);
}
