package com.sistema.sistema.infrastructure.persistence.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Native-query projection for the compact order list. */
public interface OrderListProjection {
    Long getId();
    String getOrderNumber();
    String getOrderType();
    String getTableNumber();
    String getStatus();
    Boolean getHasNotes();
    LocalDateTime getCreatedAt();
    String getCreatedByName();
    LocalDateTime getModifiedAt();
    Long getActiveItemCount();
    BigDecimal getActiveUnitCount();
    BigDecimal getTotalAmount();
    Long getPendingItemCount();
    Long getPreparingItemCount();
    Long getReadyItemCount();
    Long getServedItemCount();
    Long getCancelledItemCount();
}
