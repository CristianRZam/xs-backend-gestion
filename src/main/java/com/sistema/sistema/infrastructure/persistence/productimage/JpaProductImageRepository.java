package com.sistema.sistema.infrastructure.persistence.productimage;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface JpaProductImageRepository extends JpaRepository<ProductImageEntity, Long> {
    List<ProductImageEntity> findByProductIdAndActiveTrueOrderByOrderNumberAsc(Long productId);

    List<ProductImageEntity> findByProductId(Long productId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE ProductImageEntity p
    SET p.isMain = false
    WHERE p.productId = :productId
""")
    void clearMainByProduct(@Param("productId") Long productId);


    int countByProductIdAndActiveTrue(Long productId);

}
