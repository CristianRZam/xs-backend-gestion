package com.sistema.sistema.application.dto.request.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderChargesRequest {
    @NotNull @PositiveOrZero
    private BigDecimal packagingCharge;
    @NotNull @PositiveOrZero
    private BigDecimal deliveryCharge;
}
