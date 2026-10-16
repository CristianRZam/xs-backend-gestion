package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.domain.model.Order;
import com.sistema.sistema.domain.model.OrderItem;
import com.sistema.sistema.domain.model.OrderItemStatusHistory;
import com.sistema.sistema.application.dto.request.order.OrderItemIncidentRequest;
import com.sistema.sistema.domain.model.OrderItemIncident;

import java.util.List;
import java.time.LocalDate;
import com.sistema.sistema.application.dto.response.PageResponseDTO;
import com.sistema.sistema.application.dto.response.order.OrderListItemDTO;
import com.sistema.sistema.application.dto.response.order.OrderItemStatusHistoryDTO;

public interface OrderUseCase {

    Order create(Order order);

    Order getById(Long id);

    List<Order> getAll();
    PageResponseDTO<OrderListItemDTO> getPage(int page, int size, LocalDate fromDate, LocalDate toDate, String status, String search, Long productId);

    PageResponseDTO<Order> getKitchenQueue(int page, int size);

    Order update(Long id, Order order);

    Order updateStatus(Long id, String status);

    Order addItem(Long orderId, OrderItem item);

    Order addItems(Long orderId, List<OrderItem> items);

    Order updatePendingItem(Long orderId, Long itemId, OrderItem item);

    Order cancelPendingItem(Long orderId, Long itemId, String comment, String stockDisposition);

    Order splitPendingItem(Long orderId, Long itemId, java.math.BigDecimal quantity, String fulfillmentType);

    Order updateItemStatus(Long orderId, Long itemId, String status, String comment);
    Order updateItemsStatus(Long orderId, List<Long> itemIds, String status, String comment);
    Order reportServedItemIncident(Long orderId, Long itemId, OrderItemIncidentRequest request);
    List<OrderItemIncident> getIncidents(Long orderId);

    List<OrderItemStatusHistoryDTO> getItemStatusHistory(Long orderId, Long itemId);

    void delete(Long id);

}
