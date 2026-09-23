package com.sistema.sistema.application.dto.response.inventorymovement;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class InventoryMovementPageDTO {

    private final List<InventoryMovementDetailDTO> movements;
    private final long totalElements;
    private final int page;
    private final int size;
    private final boolean hasMore;
}
