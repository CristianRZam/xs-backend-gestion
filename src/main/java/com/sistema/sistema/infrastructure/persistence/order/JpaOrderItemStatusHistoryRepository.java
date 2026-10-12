package com.sistema.sistema.infrastructure.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderItemStatusHistoryRepository extends JpaRepository<OrderItemStatusHistoryEntity, Long> {
    List<OrderItemStatusHistoryEntity> findByOrderItemIdOrderByChangedAtAscIdAsc(Long orderItemId);
}
