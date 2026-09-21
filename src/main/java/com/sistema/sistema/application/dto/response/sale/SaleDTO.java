package com.sistema.sistema.application.dto.response.sale;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SaleDTO {
    private final Long id;
    private final String saleNumber;
    private final Long orderId;
    private final Long cashRegisterId;
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal total;
    private final String status;
    private final Long createdBy;
    private final String createdByName;
    private final LocalDateTime createdAt;
    private final List<SaleItemDTO> items;
    private final List<PaymentDTO> payments;

    @Getter @Builder
    public static class SaleItemDTO {
        private final Long productId;
        private final String productName;
        private final BigDecimal quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal discount;
        private final BigDecimal subtotal;
    }

    @Getter @Builder
    public static class PaymentDTO {
        private final String paymentMethod;
        private final BigDecimal amount;
        private final String reference;
    }
}
