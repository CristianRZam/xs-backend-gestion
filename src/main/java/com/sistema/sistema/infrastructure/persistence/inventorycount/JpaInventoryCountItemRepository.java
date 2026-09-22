package com.sistema.sistema.infrastructure.persistence.inventorycount;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface JpaInventoryCountItemRepository extends JpaRepository<InventoryCountItemEntity,Long>{ List<InventoryCountItemEntity> findBySessionIdOrderById(Long sessionId); Optional<InventoryCountItemEntity> findBySessionIdAndProductId(Long sessionId,Long productId); }
