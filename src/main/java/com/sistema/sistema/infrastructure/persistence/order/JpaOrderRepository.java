package com.sistema.sistema.infrastructure.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import com.sistema.sistema.infrastructure.persistence.order.OrderListProjection;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {
    Page<OrderEntity> findByDeletedAtIsNullOrderByCreatedAtDescIdDesc(Pageable pageable);
    Page<OrderEntity> findByDeletedAtIsNullAndCreatedAtGreaterThanEqualOrderByCreatedAtDescIdDesc(LocalDateTime fromDate, Pageable pageable);
    Page<OrderEntity> findByDeletedAtIsNullAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(LocalDateTime toDate, Pageable pageable);
    Page<OrderEntity> findByDeletedAtIsNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
    Page<OrderEntity> findByDeletedAtIsNullAndStatusOrderByCreatedAtDescIdDesc(String status, Pageable pageable);
    Page<OrderEntity> findByDeletedAtIsNullAndStatusAndCreatedAtGreaterThanEqualOrderByCreatedAtDescIdDesc(String status, LocalDateTime fromDate, Pageable pageable);
    Page<OrderEntity> findByDeletedAtIsNullAndStatusAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(String status, LocalDateTime toDate, Pageable pageable);
    Page<OrderEntity> findByDeletedAtIsNullAndStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(String status, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    Optional<OrderEntity> findByIdAndDeletedAtIsNull(Long id);

    List<OrderEntity> findByDeletedAtIsNullOrderByCreatedAtDescIdDesc();

    @Query(value = """
            SELECT o.id AS id,
                   o.order_number AS "orderNumber",
                   o.order_type AS "orderType",
                   o.table_number AS "tableNumber",
                   o.status AS status,
                   CASE WHEN o.notes IS NOT NULL AND BTRIM(o.notes) <> '' THEN true ELSE false END AS "hasNotes",
                   o.created_at AS "createdAt",
                   creator.full_name AS "createdByName",
                   o.modified_at AS "modifiedAt",
                   COUNT(DISTINCT oi.product_id) FILTER (
                       WHERE o.status = 'CANCELLED' OR oi.status <> 'CANCELLED'
                   ) AS "activeItemCount",
                   COALESCE(SUM(oi.quantity) FILTER (
                       WHERE o.status = 'CANCELLED' OR oi.status <> 'CANCELLED'
                   ), 0) AS "activeUnitCount",
                   GREATEST(
                       COALESCE(SUM(oi.quantity * oi.unit_price) FILTER (WHERE oi.status <> 'CANCELLED'), 0)
                       - COALESCE((
                           SELECT SUM(CASE
                               WHEN incident.resolution = 'CANCEL_NO_CHARGE'
                                   THEN discounted_item.quantity * discounted_item.unit_price
                               ELSE incident.discount_amount
                           END)
                           FROM order_item_incidents incident
                           JOIN order_items discounted_item
                             ON discounted_item.id = incident.order_item_id
                           WHERE incident.order_id = o.id
                             AND incident.resolution IN ('DISCOUNT', 'CANCEL_NO_CHARGE')
                             AND incident.status IN ('APPROVED', 'APROVED')
                             AND discounted_item.status <> 'CANCELLED'
                       ), 0),
                       0
                   ) + COALESCE(o.packaging_charge, 0) + COALESCE(o.delivery_charge, 0) AS "totalAmount",
                   COUNT(oi.id) FILTER (WHERE oi.status = 'PENDING') AS "pendingItemCount",
                   COUNT(oi.id) FILTER (WHERE oi.status = 'PREPARING') AS "preparingItemCount",
                   COUNT(oi.id) FILTER (WHERE oi.status = 'READY') AS "readyItemCount",
                   COUNT(oi.id) FILTER (WHERE oi.status = 'SERVED') AS "servedItemCount",
                   COUNT(oi.id) FILTER (WHERE oi.status = 'CANCELLED') AS "cancelledItemCount"
            FROM orders o
            LEFT JOIN users creator_user ON creator_user.id = o.created_by
            LEFT JOIN persons creator ON creator.id = creator_user.person_id
            LEFT JOIN order_items oi ON oi.order_id = o.id
            WHERE o.deleted_at IS NULL
              AND (CAST(:fromDate AS timestamp) IS NULL OR o.created_at >= :fromDate)
              AND (CAST(:toDateExclusive AS timestamp) IS NULL OR o.created_at < :toDateExclusive)
              AND (CAST(:status AS varchar) IS NULL OR o.status = :status)
              AND (CAST(:search AS varchar) IS NULL OR LOWER(o.order_number) LIKE CONCAT('%', LOWER(:search), '%')
                   OR LOWER(COALESCE(o.table_number, '')) LIKE CONCAT('%', LOWER(:search), '%'))
              AND (CAST(:productId AS bigint) IS NULL OR EXISTS (
                    SELECT 1 FROM order_items filtered_item
                    WHERE filtered_item.order_id = o.id AND filtered_item.product_id = :productId
              ))
            GROUP BY o.id, creator.full_name
            ORDER BY o.created_at DESC, o.id DESC
            """, countQuery = """
            SELECT COUNT(*)
            FROM orders o
            WHERE o.deleted_at IS NULL
              AND (CAST(:fromDate AS timestamp) IS NULL OR o.created_at >= :fromDate)
              AND (CAST(:toDateExclusive AS timestamp) IS NULL OR o.created_at < :toDateExclusive)
              AND (CAST(:status AS varchar) IS NULL OR o.status = :status)
              AND (CAST(:search AS varchar) IS NULL OR LOWER(o.order_number) LIKE CONCAT('%', LOWER(:search), '%')
                   OR LOWER(COALESCE(o.table_number, '')) LIKE CONCAT('%', LOWER(:search), '%'))
              AND (CAST(:productId AS bigint) IS NULL OR EXISTS (
                    SELECT 1 FROM order_items filtered_item
                    WHERE filtered_item.order_id = o.id AND filtered_item.product_id = :productId
              ))
            """, nativeQuery = true)
    Page<OrderListProjection> findListPage(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDateExclusive") LocalDateTime toDateExclusive,
            @Param("status") String status,
            @Param("search") String search,
            @Param("productId") Long productId,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT o FROM OrderEntity o JOIN o.items item
            WHERE o.deletedAt IS NULL
              AND o.status NOT IN ('COMPLETED', 'CANCELLED')
              AND item.status IN ('PENDING', 'PREPARING', 'READY')
            ORDER BY o.createdAt ASC, o.id ASC
            """)
    Page<OrderEntity> findKitchenQueue(Pageable pageable);

    boolean existsByOrderNumberAndDeletedAtIsNull(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

}
