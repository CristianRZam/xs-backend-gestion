package com.sistema.sistema.infrastructure.persistence.parameter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaParameterRepository extends JpaRepository<ParameterEntity,Long> {

    @Query("SELECT p FROM ParameterEntity p " +
            "WHERE p.deletedAt IS NULL " +
            "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:shortName IS NULL OR LOWER(p.shortName) LIKE LOWER(CONCAT('%', :shortName, '%'))) " +
            "AND (:code IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', :code, '%'))) " +
            "AND (:type IS NULL OR p.type = :type) " +
            "ORDER BY p.id ASC")
    List<ParameterEntity> findFiltered(
            @Param("name") String name,
            @Param("shortName") String shortName,
            @Param("code") String code,
            @Param("type") Long type
    );

    List<ParameterEntity> findByDeletedAtIsNullAndCodeIgnoreCaseOrderByIdAsc(String code);

    @Query("SELECT COALESCE(MAX(p.parameterId), 0) FROM ParameterEntity p WHERE LOWER(p.code) = LOWER(:code)")
    Long findMaxParameterIdByCode(@Param("code") String code);

    Optional<ParameterEntity> findByParameterIdAndCode(Long parameterId, String code);

}
