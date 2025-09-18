package com.sistema.sistema.infrastructure.persistence.userrole;

import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.domain.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserRoleDaoImpl implements UserRoleRepository {
    private final JpaUserRoleRepository jpaUserRoleRepository;
    private final UserRoleMapper mapper;

    public UserRoleDaoImpl(JpaUserRoleRepository jpaUserRoleRepository, UserRoleMapper mapper) {
        this.jpaUserRoleRepository = jpaUserRoleRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void saveUserRoles(User user, Set<Role> roles, Long assignedBy) {
        if (roles == null || roles.isEmpty()) return;

        Set<UserRoleEntity> entities = roles.stream()
                .map(role -> mapper.toEntity(user, role, assignedBy))
                .collect(Collectors.toSet());

        jpaUserRoleRepository.saveAll(entities);
    }


}
