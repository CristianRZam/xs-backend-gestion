package com.sistema.sistema.domain.repository;

import com.sistema.sistema.domain.model.Order;
import com.sistema.sistema.domain.model.OrderItem;
import com.sistema.sistema.domain.model.OrderItemStatusHistory;

import java.util.List;
import java.time.LocalDate;
import com.sistema.sistema.application.dto.response.PageResponseDTO;

public interface OrderRepository {

    Order create(Order order);

    Order getById(Long id);

    List<Order> getAll();
    PageResponseDTO<Order> getPage(int page, int size, LocalDate fromDate, LocalDate toDate, String status, String search);

    PageResponseDTO<Order> getKitchenQueue(int page, int size);

    Order update(Long id, Order order);

    Order updateStatus(Long id, String status);

    Order addItem(Long orderId, OrderItem item);

    Order updatePendingItem(Long orderId, Long itemId, OrderItem item, Long modifiedBy);

    Order cancelPendingItem(Long orderId, Long itemId, String comment, Long changedBy);

    Order splitPendingItem(Long orderId, Long itemId, java.math.BigDecimal quantity, Long changedBy);

    Order updateItemStatus(Long orderId, Long itemId, String status, String comment, Long changedBy);
    Order cancelServedItem(Long orderId, Long itemId, String comment, Long changedBy);

    List<OrderItemStatusHistory> getItemStatusHistory(Long orderId, Long itemId);

    void delete(Long id);

}
