package com.sistema.sistema.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
    @Query("SELECT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role " +
            "LEFT JOIN FETCH u.person " +
            "WHERE u.email = :email")
    Optional<UserEntity> findByEmailWithRolesAndPerson(@Param("email") String email);

    @Query("SELECT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role " +
            "LEFT JOIN FETCH u.person " +
            "WHERE u.username = :username")
    Optional<UserEntity> findByUsernameWithRolesAndPerson(@Param("username") String username);

    List<UserEntity> findByDeletedAtIsNullOrderByIdAsc();

    List<UserEntity> findByDeletedAtIsNullAndUsernameContainingIgnoreCaseOrderByIdAsc(String name);
}