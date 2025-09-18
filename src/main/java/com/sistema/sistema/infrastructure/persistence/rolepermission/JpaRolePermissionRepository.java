package com.sistema.sistema.infrastructure.persistence.rolepermission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaRolePermissionRepository extends JpaRepository<RolePermissionEntity, Long> {
    @Query("SELECT rp FROM RolePermissionEntity rp WHERE rp.role.id = :roleId")
    List<RolePermissionEntity> findRolePermissionsByRoleId(@Param("roleId") Long roleId);
}
