package com.sistema.sistema.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private Long id;
    private Long orderId;
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private String notes;
    private LocalDateTime createdAt;

}