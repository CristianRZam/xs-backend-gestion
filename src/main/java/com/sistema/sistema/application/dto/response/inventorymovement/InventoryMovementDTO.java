package com.sistema.sistema.application.dto.response.inventorymovement;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementDTO {

    private Long id;

    private Long productId;

    private String type;

    private BigDecimal quantity;

    private BigDecimal previousStock;

    private BigDecimal currentStock;

    private String reason;

    private String referenceType;

    private Long referenceId;

    private Boolean deleted;

}