package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.inventorymovement.InventoryMovementCreateRequest;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementDTO;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementDetailDTO;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementPageDTO;

import java.util.List;

public interface InventoryMovementUseCase {


    InventoryMovementPageDTO findPage(Long productId, int page, int size);

    InventoryMovementDTO findById(Long id);

    InventoryMovementDTO create(InventoryMovementCreateRequest request);

}
