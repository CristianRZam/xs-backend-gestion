package com.sistema.sistema.infrastructure.persistence.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface JpaProductRepository extends JpaRepository<ProductEntity, Long> {

    @Query("""
    SELECT p FROM ProductEntity p
    WHERE p.deletedAt IS NULL
    AND (:code IS NULL OR LOWER(CAST(p.code AS string)) LIKE LOWER(CONCAT('%', CAST(:code AS string), '%')))
    AND (:name IS NULL OR LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
    AND (:description IS NULL OR LOWER(CAST(p.description AS string)) LIKE LOWER(CONCAT('%', CAST(:description AS string), '%')))
    AND (:manageVariant IS NULL OR p.manageVariants = :manageVariant)
    AND (:status IS NULL OR p.active = :status)
    AND (:minStock IS NULL OR p.totalStock >= :minStock)
    AND (:maxStock IS NULL OR p.totalStock <= :maxStock)
    AND (:categories IS NULL OR p.categoryId IN :categories)
    AND (:unitMeasures IS NULL OR p.unitMeasureId IN :unitMeasures)
    AND (:valuationMethods IS NULL OR p.valuationMethodId IN :valuationMethods)
    ORDER BY p.id ASC
""")
    List<ProductEntity> findFilteredAll(
            @Param("code") String code,
            @Param("name") String name,
            @Param("description") String description,
            @Param("categories") List<Integer> categories,
            @Param("unitMeasures") List<Integer> unitMeasures,
            @Param("valuationMethods") List<Integer> valuationMethods,
            @Param("manageVariant") Boolean manageVariant,
            @Param("status") Boolean status,
            @Param("minStock") Long minStock,
            @Param("maxStock") Long maxStock
    );


    @Query("""
    SELECT p FROM ProductEntity p
    WHERE p.deletedAt IS NULL
    AND (:code IS NULL OR LOWER(CAST(p.code AS string)) LIKE LOWER(CONCAT('%', CAST(:code AS string), '%')))
    AND (:name IS NULL OR LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
    AND (:description IS NULL OR LOWER(CAST(p.description AS string)) LIKE LOWER(CONCAT('%', CAST(:description AS string), '%')))
    AND (:manageVariant IS NULL OR p.manageVariants = :manageVariant)
    AND (:status IS NULL OR p.active = :status)
    AND (:minStock IS NULL OR p.totalStock >= :minStock)
    AND (:maxStock IS NULL OR p.totalStock <= :maxStock)
    AND (:categories IS NULL OR p.categoryId IN :categories)
    AND (:unitMeasures IS NULL OR p.unitMeasureId IN :unitMeasures)
    AND (:valuationMethods IS NULL OR p.valuationMethodId IN :valuationMethods)
    ORDER BY p.id ASC
""")
    Page<ProductEntity> findFiltered(
            @Param("code") String code,
            @Param("name") String name,
            @Param("description") String description,
            @Param("categories") List<Integer> categories,
            @Param("unitMeasures") List<Integer> unitMeasures,
            @Param("valuationMethods") List<Integer> valuationMethods,
            @Param("manageVariant") Boolean manageVariant,
            @Param("status") Boolean status,
            @Param("minStock") Long minStock,
            @Param("maxStock") Long maxStock,
            Pageable pageable
    );


    @Query("""
    SELECT COUNT(p)
    FROM ProductEntity p
    WHERE p.deletedAt IS NULL
    AND (:status IS NULL OR p.active = :status)
    AND (:code IS NULL OR LOWER(CAST(p.code AS string)) LIKE LOWER(CONCAT('%', CAST(:code AS string), '%')))
    AND (:name IS NULL OR LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
    AND (:description IS NULL OR LOWER(CAST(p.description AS string)) LIKE LOWER(CONCAT('%', CAST(:description AS string), '%')))
    AND (:minStock IS NULL OR p.totalStock >= :minStock)
    AND (:maxStock IS NULL OR p.totalStock <= :maxStock)
    AND (:categories IS NULL OR p.categoryId IN :categories)
    AND (:unitMeasures IS NULL OR p.unitMeasureId IN :unitMeasures)
    AND (:valuationMethods IS NULL OR p.valuationMethodId IN :valuationMethods)
""")
    Long countFiltered(
            @Param("code") String code,
            @Param("name") String name,
            @Param("description") String description,
            @Param("categories") List<Integer> categories,
            @Param("unitMeasures") List<Integer> unitMeasures,
            @Param("valuationMethods") List<Integer> valuationMethods,
            @Param("status") Boolean status,
            @Param("minStock") Long minStock,
            @Param("maxStock") Long maxStock
    );

    @Query("""
    SELECT COALESCE(SUM(p.totalStock), 0)
    FROM ProductEntity p
    WHERE p.deletedAt IS NULL
    AND (:code IS NULL OR LOWER(CAST(p.code AS string)) LIKE LOWER(CONCAT('%', CAST(:code AS string), '%')))
    AND (:name IS NULL OR LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
    AND (:description IS NULL OR LOWER(CAST(p.description AS string)) LIKE LOWER(CONCAT('%', CAST(:description AS string), '%')))
    AND (:minStock IS NULL OR p.totalStock >= :minStock)
    AND (:maxStock IS NULL OR p.totalStock <= :maxStock)
    AND (:categories IS NULL OR p.categoryId IN :categories)
    AND (:unitMeasures IS NULL OR p.unitMeasureId IN :unitMeasures)
    AND (:valuationMethods IS NULL OR p.valuationMethodId IN :valuationMethods)
    AND (:status IS NULL OR p.active = :status)
""")
    BigDecimal sumFilteredStock(
            @Param("code") String code,
            @Param("name") String name,
            @Param("description") String description,
            @Param("categories") List<Integer> categories,
            @Param("unitMeasures") List<Integer> unitMeasures,
            @Param("valuationMethods") List<Integer> valuationMethods,
            @Param("status") Boolean status,
            @Param("minStock") Long minStock,
            @Param("maxStock") Long maxStock
    );


    Optional<ProductEntity> findByCode(String code);

    Optional<ProductEntity> findByCodeAndIdNot(String code, Long id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE ProductEntity p
            SET p.reservedStock = COALESCE(p.reservedStock, 0) + :quantity
            WHERE p.id = :productId
            AND p.deletedAt IS NULL
            AND p.active = true
            AND COALESCE(p.totalStock, 0) - COALESCE(p.reservedStock, 0) >= :quantity
            """)
    int reserveStock(
            @Param("productId") Long productId,
            @Param("quantity") Long quantity
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE ProductEntity p
            SET p.reservedStock = CASE
                WHEN COALESCE(p.reservedStock, 0) >= :quantity
                    THEN COALESCE(p.reservedStock, 0) - :quantity
                ELSE 0
            END
            WHERE p.id = :productId
            AND p.deletedAt IS NULL
            """)
    int releaseReservedStock(
            @Param("productId") Long productId,
            @Param("quantity") Long quantity
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE ProductEntity p SET p.totalStock = p.totalStock - :quantity
            WHERE p.id = :productId AND p.deletedAt IS NULL AND p.active = true
            AND COALESCE(p.totalStock, 0) - COALESCE(p.reservedStock, 0) >= :quantity
            """)
    int consumeAvailableStock(@Param("productId") Long productId, @Param("quantity") Long quantity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE ProductEntity p
            SET p.totalStock = p.totalStock - :quantity,
                p.reservedStock = p.reservedStock - :quantity
            WHERE p.id = :productId AND p.deletedAt IS NULL
            AND COALESCE(p.totalStock, 0) >= :quantity
            AND COALESCE(p.reservedStock, 0) >= :quantity
            """)
    int consumeReservedStock(@Param("productId") Long productId, @Param("quantity") Long quantity);

}
