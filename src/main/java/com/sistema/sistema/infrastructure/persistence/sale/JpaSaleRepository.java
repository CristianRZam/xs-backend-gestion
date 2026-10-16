package com.sistema.sistema.infrastructure.persistence.sale;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface JpaSaleRepository extends JpaRepository<SaleEntity, Long> {
    Page<SaleEntity> findByDeletedAtIsNullOrderByCreatedAtDescIdDesc(Pageable pageable);
    Page<SaleEntity> findByDeletedAtIsNullAndCreatedAtGreaterThanEqualOrderByCreatedAtDescIdDesc(LocalDateTime fromDate, Pageable pageable);
    Page<SaleEntity> findByDeletedAtIsNullAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(LocalDateTime toDate, Pageable pageable);
    Page<SaleEntity> findByDeletedAtIsNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
    Optional<SaleEntity> findByIdAndDeletedAtIsNull(Long id);
    boolean existsBySaleNumber(String saleNumber);
    boolean existsByOrderIdAndDeletedAtIsNullAndStatus(Long orderId, String status);
    List<SaleEntity> findByDeletedAtIsNullOrderByCreatedAtDescIdDesc();

    @Query(value = """
            SELECT s.id AS id, s.sale_number AS saleNumber, s.order_id AS orderId,
                   o.order_number AS orderNumber,
                   s.total AS total, s.status AS status, s.notes AS notes,
                   p.full_name AS createdByName, s.created_at AS createdAt,
                   COALESCE((SELECT COUNT(*) FROM sale_items si WHERE si.sale_id = s.id), 0) AS itemCount,
                   COALESCE((SELECT SUM(si.quantity) FROM sale_items si WHERE si.sale_id = s.id), 0) AS totalQuantity,
                   COALESCE((SELECT string_agg(DISTINCT py.payment_method, ',' ORDER BY py.payment_method)
                             FROM payments py WHERE py.sale_id = s.id), '') AS paymentMethods
            FROM sales s
            LEFT JOIN orders o ON o.id = s.order_id
            LEFT JOIN users u ON u.id = s.created_by
            LEFT JOIN persons p ON p.id = u.person_id
            WHERE s.deleted_at IS NULL
              AND (CAST(:fromDate AS timestamp) IS NULL OR s.created_at >= CAST(:fromDate AS timestamp))
              AND (CAST(:toDate AS timestamp) IS NULL OR s.created_at < CAST(:toDate AS timestamp))
              AND (CAST(:status AS varchar) IS NULL OR s.status = CAST(:status AS varchar))
              AND (CAST(:productId AS bigint) IS NULL OR EXISTS (
                    SELECT 1 FROM sale_items filter_item
                    WHERE filter_item.sale_id = s.id AND filter_item.product_id = CAST(:productId AS bigint)
              ))
            ORDER BY s.created_at DESC, s.id DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM sales s
            WHERE s.deleted_at IS NULL
              AND (CAST(:fromDate AS timestamp) IS NULL OR s.created_at >= CAST(:fromDate AS timestamp))
              AND (CAST(:toDate AS timestamp) IS NULL OR s.created_at < CAST(:toDate AS timestamp))
              AND (CAST(:status AS varchar) IS NULL OR s.status = CAST(:status AS varchar))
              AND (CAST(:productId AS bigint) IS NULL OR EXISTS (
                    SELECT 1 FROM sale_items filter_item
                    WHERE filter_item.sale_id = s.id AND filter_item.product_id = CAST(:productId AS bigint)
              ))
            """, nativeQuery = true)
    Page<SaleListItemProjection> findListPage(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("status") String status,
            @Param("productId") Long productId,
            Pageable pageable
    );

    @Query(value = """
            SELECT p.full_name AS createdByName
            FROM sales s
            INNER JOIN users u ON u.id = s.created_by
            INNER JOIN persons p ON p.id = u.person_id
            WHERE s.id = :saleId
            """, nativeQuery = true)
    Optional<SaleUserNameProjection> findCreatedByName(@Param("saleId") Long saleId);

    @Query("SELECT DISTINCT s FROM SaleEntity s JOIN s.payments p WHERE p.cashSessionId = :sessionId AND s.deletedAt IS NULL AND s.status = 'COMPLETED' ORDER BY s.createdAt DESC, s.id DESC")
    List<SaleEntity> findCompletedByCashSessionId(@Param("sessionId") Long sessionId);

    @Query(value = "SELECT name FROM products WHERE id = :productId", nativeQuery = true)
    String findProductName(@Param("productId") Long productId);

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM payments p
                INNER JOIN cash_sessions cs ON cs.id = p.cash_session_id
                WHERE p.sale_id = :saleId
                  AND cs.status = 'OPEN'
                  AND cs.deleted_at IS NULL
            )
            """, nativeQuery = true)
    boolean isOriginalCashSessionOpen(@Param("saleId") Long saleId);
}
