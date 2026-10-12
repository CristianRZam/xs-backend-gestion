package com.sistema.sistema.domain.repository;

import com.sistema.sistema.domain.model.OrderItemIncident;
import java.util.List;

public interface OrderItemIncidentRepository {
    OrderItemIncident save(OrderItemIncident incident);
    List<OrderItemIncident> findByOrderId(Long orderId);
    void supersedeApprovedDiscounts(Long orderId, Long orderItemId);
}
