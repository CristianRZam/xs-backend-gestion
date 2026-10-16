package com.sistema.sistema.infrastructure.persistence.inventorycount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaInventoryCountSessionRepository
        extends JpaRepository<InventoryCountSessionEntity, Long> {

    Optional<InventoryCountSessionEntity> findFirstByStatusInOrderByOpenedAtDescIdDesc(
            List<String> statuses
    );

    List<InventoryCountSessionEntity> findAllByOrderByOpenedAtDescIdDesc();

    Page<InventoryCountSessionEntity> findAllByOrderByOpenedAtDescIdDesc(Pageable pageable);

    List<InventoryCountSessionEntity> findByStatusInAndOpenedAtBefore(
            List<String> statuses,
            LocalDateTime openedBefore
    );

    boolean existsByCountNumber(String countNumber);
}
