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
