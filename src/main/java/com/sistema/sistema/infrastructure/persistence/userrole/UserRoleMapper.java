package com.sistema.sistema.infrastructure.persistence.userrole;

import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.infrastructure.persistence.role.RoleEntity;
import com.sistema.sistema.infrastructure.persistence.user.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserRoleMapper {

    public UserRoleEntity toEntity(User userDomain, Role roleDomain, Long assignedBy) {
        if (userDomain.getId() == null) {
            throw new IllegalArgumentException("El usuario aún no tiene ID persistido");
        }
        if (roleDomain.getId() == null) {
            throw new IllegalArgumentException("El rol aún no tiene ID persistido");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userDomain.getId());

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(roleDomain.getId());

        return UserRoleEntity.builder()
                .user(userEntity)
                .role(roleEntity)
                .assignedBy(assignedBy)
                .createdBy(assignedBy)
                .build();
    }
}
