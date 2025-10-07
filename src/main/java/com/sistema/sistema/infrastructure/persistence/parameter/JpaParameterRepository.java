package com.sistema.sistema.infrastructure.persistence.parameter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaParameterRepository extends JpaRepository<ParameterEntity,Long> {
    List<ParameterEntity> findByDeletedAtIsNullAndCodeIgnoreCaseOrderByIdAsc(String code);

    @Query("SELECT COALESCE(MAX(p.parameterId), 0) FROM ParameterEntity p WHERE LOWER(p.code) = LOWER(:code)")
    Long findMaxParameterIdByCode(@Param("code") String code);

    Optional<ParameterEntity> findByParameterIdAndCode(Long parameterId, String code);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END " +
            "FROM ParameterEntity p " +
            "WHERE p.parameterId = :parameterId " +
            "AND p.code = :code " +
            "AND p.deletedAt IS NULL " +
            "AND p.active = TRUE")
    boolean existsActiveAndNotDeletedParameter(@Param("parameterId") Long parameterId, @Param("code") String code);



    List<ParameterEntity> findByCodeIgnoreCaseOrderByIdAsc(String code);
}
