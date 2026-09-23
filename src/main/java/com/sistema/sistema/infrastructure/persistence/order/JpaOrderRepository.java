package com.sistema.sistema.infrastructure.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long> {
    Page<OrderEntity> findByDeletedAtIsNullOrderByCreatedAtDescIdDesc(Pageable pageable);
    Page<OrderEntity> findByDeletedAtIsNullAndCreatedAtGreaterThanEqualOrderByCreatedAtDescIdDesc(LocalDateTime fromDate, Pageable pageable);
    Page<OrderEntity> findByDeletedAtIsNullAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(LocalDateTime toDate, Pageable pageable);
    Page<OrderEntity> findByDeletedAtIsNullAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    Optional<OrderEntity> findByIdAndDeletedAtIsNull(Long id);

    List<OrderEntity> findByDeletedAtIsNullOrderByCreatedAtDescIdDesc();

    boolean existsByOrderNumberAndDeletedAtIsNull(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

}
