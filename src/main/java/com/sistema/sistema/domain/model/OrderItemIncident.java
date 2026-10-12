package com.sistema.sistema.domain.model;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class OrderItemIncident {
    Long id;
    Long orderId;
    Long orderItemId;
    Long productId;
    String resolution;
    String reason;
    BigDecimal discountAmount;
    Long reportedBy;
    LocalDateTime reportedAt;
    String status;
    Long approvedBy;
    LocalDateTime approvedAt;
}
