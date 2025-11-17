package com.sistema.sistema.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Long categoryId;
    private Long unitMeasureId;
    private Long valuationMethodId;
    private Boolean manageVariants;
    private BigDecimal basePrice;
    private BigDecimal promoPrice;
    private BigDecimal baseCost;
    private Long totalStock;
    private Boolean active;

    // Auditoría
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long modifiedBy;
    private LocalDateTime modifiedAt;
    private Long deletedBy;
    private LocalDateTime deletedAt;
}
