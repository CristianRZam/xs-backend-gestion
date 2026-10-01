package com.sistema.sistema.application.dto.response.dashboard;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DashboardDTO {
    @Builder.Default
    private final String scope = "GLOBAL";
    private final LocalDate summaryDate;
    private final Long todaySalesCount;
    private final BigDecimal averageSale;
    private final BigDecimal todaySales;
    private final Long todayOrders;
    private final List<DailySalesDTO> weeklySales;
    private final List<ProductSalesDTO> topProducts;
    private final List<PaymentMethodDTO> paymentMethods;
}
