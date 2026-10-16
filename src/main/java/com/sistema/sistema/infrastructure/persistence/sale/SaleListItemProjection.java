package com.sistema.sistema.infrastructure.persistence.sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SaleListItemProjection {
    Long getId();
    String getSaleNumber();
    Long getOrderId();
    String getOrderNumber();
    BigDecimal getTotal();
    String getStatus();
    String getNotes();
    String getCreatedByName();
    LocalDateTime getCreatedAt();
    Long getItemCount();
    BigDecimal getTotalQuantity();
    String getPaymentMethods();
}
