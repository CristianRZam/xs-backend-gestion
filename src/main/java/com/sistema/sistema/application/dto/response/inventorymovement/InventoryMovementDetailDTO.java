package com.sistema.sistema.application.dto.response.inventorymovement;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementDetailDTO {

    // Movimiento
    private Long id;
    private String type;
    private BigDecimal quantity;
    private BigDecimal previousStock;
    private BigDecimal currentStock;
    private String reason;
    private String referenceType;
    private Long referenceId;
    private Boolean deleted;

    // Producto
    private Long productId;
    private String productCode;
    private String productName;

    // Auditoría
    private LocalDateTime createdAt;
    private String createdBy;

    private LocalDateTime modifiedAt;
    private String modifiedBy;

    private LocalDateTime deletedAt;
    private String deletedBy;
}