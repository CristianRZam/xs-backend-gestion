package com.sistema.sistema.application.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long categoryId;
    private String nameCategory;
    private Long unitMeasureId;
    private String nameUnitMeasure;
    private Long valuationMethodId;
    private String nameValuationMethod;
    private Boolean manageVariants;
    private BigDecimal basePrice;
    private BigDecimal promoPrice;
    private BigDecimal baseCost;
    private Long totalStock;
    private Boolean active;
    private Boolean deleted;
}
