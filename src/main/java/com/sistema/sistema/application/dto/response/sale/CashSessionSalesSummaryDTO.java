package com.sistema.sistema.application.dto.response.sale;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CashSessionSalesSummaryDTO {
    private final Long cashSessionId;
    private final BigDecimal totalSold;
    private final List<PaymentMethodTotalDTO> paymentMethods;
    private final List<SaleDTO> sales;

    @Getter
    @Builder
    public static class PaymentMethodTotalDTO {
        private final String paymentMethod;
        private final BigDecimal total;
    }
}
