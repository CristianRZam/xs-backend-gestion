package com.sistema.sistema.infrastructure.persistence.userrole;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaUserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    List<UserRoleEntity> findAllByUser_Id(Long userId);
}
