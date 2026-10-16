package com.sistema.sistema.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
    @Query("SELECT DISTINCT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role r " +
            "LEFT JOIN FETCH r.rolePermissions rp " +
            "LEFT JOIN FETCH rp.permission p " +
            "LEFT JOIN FETCH u.person " +
            "WHERE u.email = :email " +
            "AND r.deletedAt IS NULL  " +
            "AND u.deletedAt IS NULL")
    Optional<UserEntity> findByEmailWithRolesAndPerson(@Param("email") String email);


    @Query("SELECT DISTINCT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role r " +
            "LEFT JOIN FETCH r.rolePermissions rp " +
            "LEFT JOIN FETCH rp.permission p " +
            "LEFT JOIN FETCH u.person " +
            "WHERE u.username = :username " +
            "AND r.deletedAt IS NULL  " +
            "AND u.deletedAt IS NULL")
    Optional<UserEntity> findByUsernameWithRolesAndPerson(@Param("username") String username);

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.person WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<UserEntity> findByIdWithPerson(@Param("id") Long id);

    @Query("SELECT DISTINCT u FROM UserEntity u " +
            "JOIN u.userRoles ur " +
            "JOIN ur.role r " +
            "JOIN r.rolePermissions rp " +
            "JOIN rp.permission p " +
            "WHERE u.active = TRUE " +
            "AND u.deletedAt IS NULL " +
            "AND ur.deletedAt IS NULL " +
            "AND r.active = TRUE " +
            "AND r.deletedAt IS NULL " +
            "AND rp.deletedAt IS NULL " +
            "AND p.deletedAt IS NULL " +
            "AND p.name = :permissionName")
    List<UserEntity> findActiveUsersWithPermission(@Param("permissionName") String permissionName);

}
