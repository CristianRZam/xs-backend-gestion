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
    @Getter @Builder public static class DailySalesDTO { private final LocalDate date; private final BigDecimal total; }
    @Getter @Builder public static class ProductSalesDTO { private final Long productId; private final String productName; private final Long quantity; }
    @Getter @Builder public static class PaymentMethodDTO { private final String method; private final BigDecimal total; }
}
