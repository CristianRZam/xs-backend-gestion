package com.sistema.sistema.domain.repository;

import com.sistema.sistema.application.dto.request.inventorymovement.InventoryMovementCreateRequest;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementDTO;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementDetailDTO;

import java.util.List;

public interface IventoryMovementRepository {
    List<InventoryMovementDetailDTO> findAll(Long productId);

    InventoryMovementDTO create(InventoryMovementCreateRequest request);

    InventoryMovementDTO findById(Long id);
}
