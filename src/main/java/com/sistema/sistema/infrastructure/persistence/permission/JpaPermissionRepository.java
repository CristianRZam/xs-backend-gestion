package com.sistema.sistema.infrastructure.persistence.permission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface JpaPermissionRepository extends JpaRepository<PermissionEntity, Long> {

    List<PermissionEntity> findByDeletedAtIsNullOrderByIdAsc();


    @Query("SELECT p FROM PermissionEntity p JOIN RolePermissionEntity rp ON rp.permission = p JOIN rp.role r WHERE r.id = :roleId AND p.deletedAt IS NULL AND rp.deletedAt IS NULL ORDER BY p.id ASC")
    List<PermissionEntity> findByRoleId(Long roleId);


}
