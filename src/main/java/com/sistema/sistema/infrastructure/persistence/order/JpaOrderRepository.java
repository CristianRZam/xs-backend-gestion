package com.sistema.sistema.infrastructure.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByIdAndDeletedAtIsNull(Long id);

    List<OrderEntity> findByDeletedAtIsNullOrderByCreatedAtDescIdDesc();

    boolean existsByOrderNumberAndDeletedAtIsNull(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

}
