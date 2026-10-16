package com.sistema.sistema.application.dto.response.sale;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Datos compactos para el listado; el detalle completo se consulta por id. */
@Getter
@Builder
public class SaleListItemDTO {
    private final Long id;
    private final String saleNumber;
    private final Long orderId;
    private final String orderNumber;
    private final BigDecimal total;
    private final String status;
    private final String notes;
    private final String createdByName;
    private final LocalDateTime createdAt;
    private final Long itemCount;
    private final BigDecimal totalQuantity;
    private final List<String> paymentMethods;
}
