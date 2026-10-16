package com.sistema.sistema.application.dto.response.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Compact read model used exclusively by the paginated order list.
 *
 * <p>It deliberately excludes order items and other detail-only data so the
 * mobile list can be rendered without loading an order aggregate per row.</p>
 */
@Getter
@Builder
public class OrderListItemDTO {
    private final Long id;
    private final String orderNumber;
    private final String orderType;
    private final String tableNumber;
    private final String status;
    private final boolean hasNotes;
    private final LocalDateTime createdAt;
    private final String createdByName;
    private final LocalDateTime modifiedAt;
    private final Long activeItemCount;
    private final BigDecimal activeUnitCount;
    private final BigDecimal totalAmount;
    private final Long pendingItemCount;
    private final Long preparingItemCount;
    private final Long readyItemCount;
    private final Long servedItemCount;
    private final Long cancelledItemCount;
}
