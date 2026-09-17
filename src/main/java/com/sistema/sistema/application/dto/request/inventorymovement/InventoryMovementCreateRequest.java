package com.sistema.sistema.application.dto.request.inventorymovement;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementCreateRequest {

    private Long productId;

    private String type;

    private BigDecimal quantity;

    private BigDecimal previousStock;

    private BigDecimal currentStock;

    private String reason;

    private String referenceType;

    private Long referenceId;

}