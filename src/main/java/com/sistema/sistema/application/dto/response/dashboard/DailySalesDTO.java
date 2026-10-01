package com.sistema.sistema.application.dto.response.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DailySalesDTO {
    private final LocalDate date;
    private final BigDecimal total;
}
