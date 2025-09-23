package com.sistema.sistema.infrastructure.persistence.user;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
    @Query("SELECT DISTINCT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role r " +
            "LEFT JOIN FETCH r.permissions p " +
            "LEFT JOIN RolePermissionEntity rp ON rp.role = r AND rp.permission = p " +
            "LEFT JOIN FETCH u.person " +
            "WHERE u.email = :email " +
            "AND (rp.deletedAt IS NULL OR rp IS NULL) " +
            "AND u.deletedAt IS NULL")
    Optional<UserEntity> findByEmailWithRolesAndPerson(@Param("email") String email);

    @Query("SELECT DISTINCT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role r " +
            "LEFT JOIN FETCH r.permissions p " +
            "LEFT JOIN RolePermissionEntity rp ON rp.role = r AND rp.permission = p " +
            "LEFT JOIN FETCH u.person " +
            "WHERE u.username = :username " +
            "AND (rp.deletedAt IS NULL OR rp IS NULL) " +
            "AND u.deletedAt IS NULL")
    Optional<UserEntity> findByUsernameWithRolesAndPerson(@Param("username") String username);

    List<UserEntity> findByDeletedAtIsNullOrderByIdAsc();
    List<UserEntity> findByDeletedAtIsNullAndUsernameContainingIgnoreCaseOrderByIdAsc(String name);

}