package com.sistema.sistema.infrastructure.persistence.inventorycount;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaInventoryCountSessionRepository
        extends JpaRepository<InventoryCountSessionEntity, Long> {

    Optional<InventoryCountSessionEntity> findFirstByBusinessDateAndStatusInOrderByIdDesc(
            LocalDate date,
            List<String> statuses
    );

    List<InventoryCountSessionEntity> findAllByOrderByOpenedAtDescIdDesc();

    List<InventoryCountSessionEntity> findByStatusInAndOpenedAtBefore(
            List<String> statuses,
            LocalDateTime openedBefore
    );

    boolean existsByCountNumber(String countNumber);
}
