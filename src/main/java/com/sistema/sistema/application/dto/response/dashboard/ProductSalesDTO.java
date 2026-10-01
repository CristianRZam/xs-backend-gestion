package com.sistema.sistema.application.dto.response.dashboard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductSalesDTO {
    private final Long productId;
    private final String productName;
    private final Long quantity;
}
