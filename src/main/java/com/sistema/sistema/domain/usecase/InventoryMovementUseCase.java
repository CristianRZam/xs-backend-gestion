package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.inventorymovement.InventoryMovementCreateRequest;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementDTO;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementDetailDTO;

import java.util.List;

public interface InventoryMovementUseCase {


    List<InventoryMovementDetailDTO> findAll(Long productId);

    InventoryMovementDTO findById(Long id);

    InventoryMovementDTO create(InventoryMovementCreateRequest request);

}