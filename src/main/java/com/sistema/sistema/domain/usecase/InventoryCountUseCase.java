package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.inventorycount.InventoryCountCloseRequest;
import com.sistema.sistema.infrastructure.persistence.inventorycount.InventoryCountSessionEntity;
import java.util.List;
import java.util.Map;

public interface InventoryCountUseCase {
    InventoryCountSessionEntity open(String comment);
    InventoryCountSessionEntity current();
    List<InventoryCountSessionEntity> history();
    Map<String, Object> detail(Long id);
    Map<String, Object> review(Long id, InventoryCountCloseRequest request);
    InventoryCountSessionEntity close(Long id, InventoryCountCloseRequest request);
}
