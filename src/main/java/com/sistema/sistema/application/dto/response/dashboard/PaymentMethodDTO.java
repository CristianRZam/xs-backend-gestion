package com.sistema.sistema.application.dto.response.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentMethodDTO {
    private final String method;
    private final BigDecimal total;
}
