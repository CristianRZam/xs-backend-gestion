package com.sistema.sistema.infrastructure.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaOrderItemStatusHistoryRepository extends JpaRepository<OrderItemStatusHistoryEntity, Long> {
    List<OrderItemStatusHistoryEntity> findByOrderItemIdOrderByChangedAtAscIdAsc(Long orderItemId);

    @Query(value = """
            SELECT history.id AS id,
                   history.order_item_id AS \"orderItemId\",
                   history.previous_status AS \"previousStatus\",
                   history.new_status AS \"newStatus\",
                   history.comment AS comment,
                   history.changed_by AS \"changedBy\",
                   person.full_name AS \"changedByName\",
                   history.changed_at AS \"changedAt\"
            FROM order_item_status_history history
            LEFT JOIN users changed_by_user ON changed_by_user.id = history.changed_by
            LEFT JOIN persons person ON person.id = changed_by_user.person_id
            WHERE history.order_item_id = :itemId
            ORDER BY history.changed_at ASC, history.id ASC
            """, nativeQuery = true)
    List<OrderItemStatusHistoryProjection> findHistoryWithActorByOrderItemId(@Param("itemId") Long itemId);
}
