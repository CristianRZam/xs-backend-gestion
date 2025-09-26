package com.sistema.sistema.infrastructure.persistence.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaRoleRepository extends JpaRepository<RoleEntity, Long> {
}
