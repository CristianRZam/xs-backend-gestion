package com.sistema.sistema.infrastructure.persistence.order;

import java.time.LocalDateTime;

/** Projection used to include the person who performed each status change. */
public interface OrderItemStatusHistoryProjection {
    Long getId();
    Long getOrderItemId();
    String getPreviousStatus();
    String getNewStatus();
    String getComment();
    Long getChangedBy();
    String getChangedByName();
    LocalDateTime getChangedAt();
}
