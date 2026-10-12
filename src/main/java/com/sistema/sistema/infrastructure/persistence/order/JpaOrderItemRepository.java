package com.sistema.sistema.infrastructure.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;

interface JpaOrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
}
