package com.sistema.sistema.application.dto.response.order;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class OrderItemIncidentResponse {
    Long id;
    Long orderId;
    Long orderItemId;
    Long productId;
    String resolution;
    String reason;
    BigDecimal discountAmount;
    String status;
    Long reportedBy;
    LocalDateTime reportedAt;
    Long approvedBy;
    LocalDateTime approvedAt;
}
