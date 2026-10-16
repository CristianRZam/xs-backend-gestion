package com.sistema.sistema.application.dto.request.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @Pattern(regexp = "DINE_IN|TAKEAWAY|DELIVERY", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "El tipo de entrega no es válido.")
    private String fulfillmentType;
}
