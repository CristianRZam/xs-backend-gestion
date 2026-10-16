package com.sistema.sistema.application.dto.response.order;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Read model for the audit trail of a single order item.
 *
 * <p>The actor name is resolved at query time so clients do not need to make
 * one request per historical entry.</p>
 */
@Getter
@Builder
public class OrderItemStatusHistoryDTO {
    private final Long id;
    private final Long orderItemId;
    private final String previousStatus;
    private final String newStatus;
    private final String comment;
    private final Long changedBy;
    private final String changedByName;
    private final LocalDateTime changedAt;
}
