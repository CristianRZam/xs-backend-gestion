package com.sistema.sistema.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemStatusHistory {
    private Long id;
    private Long orderItemId;
    private String previousStatus;
    private String newStatus;
    private String comment;
    private Long changedBy;
    private String changedByName;
    private LocalDateTime changedAt;
}
