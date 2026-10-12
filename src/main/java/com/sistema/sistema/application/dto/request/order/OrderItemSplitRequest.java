package com.sistema.sistema.application.dto.request.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemSplitRequest {
    @NotNull
    @Positive
    private BigDecimal quantity;
}
