package com.sistema.sistema.infrastructure.persistence.inventorycount;
import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDate; import java.util.*;
public interface JpaInventoryCountSessionRepository extends JpaRepository<InventoryCountSessionEntity,Long>{ Optional<InventoryCountSessionEntity> findFirstByBusinessDateAndStatusInOrderByIdDesc(LocalDate date,List<String> statuses); List<InventoryCountSessionEntity> findAllByOrderByOpenedAtDescIdDesc(); boolean existsByCountNumber(String countNumber); }
