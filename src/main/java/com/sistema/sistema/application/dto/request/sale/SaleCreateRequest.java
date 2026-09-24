package com.sistema.sistema.application.dto.request.sale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SaleCreateRequest {
    private Long orderId;
    // El servidor genera el número con la fecha y hora de registro.
    private String saleNumber;
    @PositiveOrZero private BigDecimal discount = BigDecimal.ZERO;
    @Valid @NotEmpty private List<SaleItemRequest> items;
    @Valid @NotEmpty private List<PaymentRequest> payments;

    @Getter @Setter
    public static class SaleItemRequest {
        @NotNull @Positive private Long productId;
        @NotNull @Positive private BigDecimal quantity;
        @NotNull @PositiveOrZero private BigDecimal unitPrice;
        @PositiveOrZero private BigDecimal discount = BigDecimal.ZERO;
    }

    @Getter @Setter
    public static class PaymentRequest {
        @NotBlank private String paymentMethod;
        @NotNull @Positive private BigDecimal amount;
        @Positive private BigDecimal receivedAmount;
        private String reference;
    }
}
