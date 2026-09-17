package com.sistema.sistema.infrastructure.persistence.inventorymovement;

import com.sistema.sistema.application.dto.request.inventorymovement.InventoryMovementCreateRequest;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementDTO;
import org.springframework.stereotype.Component;

@Component
public class InventoryMovementMapper {

    public InventoryMovementDTO toDTOFromEntity(InventoryMovementEntity entity) {

        if (entity == null) {
            return null;
        }

        return InventoryMovementDTO.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .type(entity.getType())
                .quantity(entity.getQuantity())
                .previousStock(entity.getPreviousStock())
                .currentStock(entity.getCurrentStock())
                .reason(entity.getReason())
                .referenceType(entity.getReferenceType())
                .referenceId(entity.getReferenceId())
                .deleted(entity.getDeletedAt() != null)
                .build();
    }

    public InventoryMovementEntity toEntity(InventoryMovementCreateRequest request) {

        if (request == null) {
            return null;
        }

        return InventoryMovementEntity.builder()
                .productId(request.getProductId())
                .type(request.getType())
                .quantity(request.getQuantity())
                .previousStock(request.getPreviousStock())
                .currentStock(request.getCurrentStock())
                .reason(request.getReason())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .build();
    }


}