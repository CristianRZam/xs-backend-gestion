package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.PageResponseDTO;
import com.sistema.sistema.application.dto.request.order.OrderItemStatusRequest;
import com.sistema.sistema.application.dto.request.order.OrderItemsStatusRequest;
import com.sistema.sistema.application.dto.request.order.OrderItemSplitRequest;
import java.time.LocalDate;
import com.sistema.sistema.domain.model.Order;
import com.sistema.sistema.domain.model.OrderItem;
import com.sistema.sistema.domain.model.OrderItemStatusHistory;
import com.sistema.sistema.application.dto.request.order.OrderItemIncidentRequest;
import com.sistema.sistema.domain.model.OrderItemIncident;
import com.sistema.sistema.application.dto.response.order.OrderItemIncidentResponse;
import com.sistema.sistema.domain.usecase.OrderUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private final OrderUseCase orderUseCase;

    public OrderController(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    // ==========================================================
    // CREAR ORDEN
    // ==========================================================

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ORDER')")
    public ResponseEntity<ApiResponse<Order>> create(@RequestBody Order request) {

        Order data = orderUseCase.create(request);
        return ApiResponseFactory.created(data, "Orden creada correctamente");

    }

    // ==========================================================
    // OBTENER ORDEN POR ID
    // ==========================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_ORDER')")
    public ResponseEntity<ApiResponse<Order>> getById(@PathVariable Long id) {

        Order data = orderUseCase.getById(id);
        return ApiResponseFactory.success(data, "Orden obtenida correctamente");

    }

    // ==========================================================
    // OBTENER TODAS LAS ORDENES
    // ==========================================================

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ORDER')")
    public ResponseEntity<ApiResponse<PageResponseDTO<Order>>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate, @RequestParam(required = false) String status, @RequestParam(required = false) String search) {

        PageResponseDTO<Order> data = orderUseCase.getPage(page, size, fromDate, toDate, status, search);
        return ApiResponseFactory.success(data, "Ordenes obtenidas correctamente");

    }

    @GetMapping("/kitchen-queue")
    @PreAuthorize("hasAuthority('VIEW_KITCHEN_QUEUE')")
    public ResponseEntity<ApiResponse<PageResponseDTO<Order>>> getKitchenQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponseFactory.success(
                orderUseCase.getKitchenQueue(page, size),
                "Cola de cocina obtenida correctamente"
        );
    }

    // ==========================================================
    // ACTUALIZAR ORDEN
    // ==========================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDIT_ORDER')")
    public ResponseEntity<ApiResponse<Order>> update(@PathVariable Long id, @RequestBody Order request) {

        Order data = orderUseCase.update(id, request);
        return ApiResponseFactory.success(data, "Orden actualizada correctamente");

    }

    // ==========================================================
    // ACTUALIZAR ESTADO
    // ==========================================================

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('UPDATE_ORDER_STATUS')")
    public ResponseEntity<ApiResponse<Order>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {

        String status = request.get("status");
        Order data = orderUseCase.updateStatus(id, status);
        return ApiResponseFactory.success(data, "Estado de la orden actualizado correctamente");

    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('ADD_ORDER_ITEM')")
    public ResponseEntity<ApiResponse<Order>> addItem(
            @PathVariable Long id,
            @Valid @RequestBody OrderItem request
    ) {
        log.info("Adding item to order {}", id);
        Order data = orderUseCase.addItem(id, request);
        return ApiResponseFactory.success(data, "Producto agregado a la orden correctamente");
    }

    @PutMapping("/{orderId}/items/{itemId}")
    @PreAuthorize("hasAuthority('EDIT_PENDING_ORDER_ITEM')")
    public ResponseEntity<ApiResponse<Order>> updatePendingItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestBody OrderItem request
    ) {
        log.info("Updating pending item {} in order {}", itemId, orderId);
        return ApiResponseFactory.success(
                orderUseCase.updatePendingItem(orderId, itemId, request),
                "Producto pendiente actualizado correctamente"
        );
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @PreAuthorize("hasAuthority('CANCEL_ORDER_ITEM')")
    public ResponseEntity<ApiResponse<Order>> cancelPendingItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestParam(required = false) String comment,
            @RequestParam(defaultValue = "RETURN_TO_STOCK") String stockDisposition
    ) {
        log.info("Cancelling active item {} in order {} with disposition {}", itemId, orderId, stockDisposition);
        return ApiResponseFactory.success(
                orderUseCase.cancelPendingItem(orderId, itemId, comment, stockDisposition),
                "Producto cancelado correctamente"
        );
    }

    @PostMapping("/{orderId}/items/{itemId}/split")
    @PreAuthorize("hasAuthority('EDIT_PENDING_ORDER_ITEM')")
    public ResponseEntity<ApiResponse<Order>> splitPendingItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @Valid @RequestBody OrderItemSplitRequest request
    ) {
        log.info("Splitting item {} in order {}", itemId, orderId);
        return ApiResponseFactory.success(
                orderUseCase.splitPendingItem(orderId, itemId, request.getQuantity()),
                "Producto dividido correctamente"
        );
    }

    @PutMapping("/{orderId}/items/{itemId}/status")
    @PreAuthorize("hasAuthority('CORRECT_ORDER_ITEM_STATUS') or (#request != null and #request.status != null and "
            + "((#request.status.equalsIgnoreCase('PREPARING') and hasAuthority('PREPARE_ORDER_ITEM')) or "
            + "(#request.status.equalsIgnoreCase('READY') and hasAuthority('MARK_ORDER_ITEM_READY')) or "
            + "(#request.status.equalsIgnoreCase('SERVED') and hasAuthority('SERVE_ORDER_ITEM'))))")
    public ResponseEntity<ApiResponse<Order>> updateItemStatus(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @Valid @RequestBody OrderItemStatusRequest request
    ) {
        log.info("Updating status of order item {} in order {}", itemId, orderId);
        Order data = orderUseCase.updateItemStatus(orderId, itemId, request.getStatus(), request.getComment());
        return ApiResponseFactory.success(data, "Estado del producto actualizado correctamente");
    }

    @PutMapping("/{orderId}/items/status")
    @PreAuthorize("hasAuthority('CORRECT_ORDER_ITEM_STATUS') or hasAuthority('PREPARE_ORDER_ITEM') or hasAuthority('MARK_ORDER_ITEM_READY') or hasAuthority('SERVE_ORDER_ITEM')")
    public ResponseEntity<ApiResponse<Order>> updateItemsStatus(@PathVariable Long orderId, @Valid @RequestBody OrderItemsStatusRequest request) {
        return ApiResponseFactory.success(orderUseCase.updateItemsStatus(orderId, request.getItemIds(), request.getStatus(), request.getComment()), "Productos actualizados correctamente");
    }

    @GetMapping("/{orderId}/items/{itemId}/status-history")
    @PreAuthorize("hasAuthority('VIEW_ORDER_ITEM_HISTORY')")
    public ResponseEntity<ApiResponse<List<OrderItemStatusHistory>>> getItemStatusHistory(
            @PathVariable Long orderId,
            @PathVariable Long itemId
    ) {
        List<OrderItemStatusHistory> data = orderUseCase.getItemStatusHistory(orderId, itemId);
        return ApiResponseFactory.success(data, "Historial del producto obtenido correctamente");
    }

    @PostMapping("/{orderId}/items/{itemId}/incidents")
    @PreAuthorize("hasAuthority('REPORT_ORDER_INCIDENT') and hasAuthority('APPROVE_ORDER_COMPENSATION')")
    public ResponseEntity<ApiResponse<Order>> reportServedItemIncident(@PathVariable Long orderId, @PathVariable Long itemId,
            @Valid @RequestBody OrderItemIncidentRequest request) {
        return ApiResponseFactory.success(orderUseCase.reportServedItemIncident(orderId, itemId, request), "Incidencia registrada correctamente");
    }

    @GetMapping("/{orderId}/incidents")
    @PreAuthorize("hasAuthority('VIEW_ORDER_ITEM_HISTORY')")
    public ResponseEntity<ApiResponse<List<OrderItemIncidentResponse>>> getIncidents(@PathVariable Long orderId) {
        List<OrderItemIncidentResponse> incidents = orderUseCase.getIncidents(orderId).stream().map(this::toIncidentResponse).toList();
        return ApiResponseFactory.success(incidents, "Historial de incidencias obtenido correctamente");
    }

    private OrderItemIncidentResponse toIncidentResponse(OrderItemIncident incident) {
        return OrderItemIncidentResponse.builder().id(incident.getId()).orderId(incident.getOrderId())
                .orderItemId(incident.getOrderItemId()).productId(incident.getProductId()).resolution(incident.getResolution())
                .reason(incident.getReason()).discountAmount(incident.getDiscountAmount()).status(incident.getStatus())
                .reportedBy(incident.getReportedBy()).reportedAt(incident.getReportedAt()).approvedBy(incident.getApprovedBy())
                .approvedAt(incident.getApprovedAt()).build();
    }

    // ==========================================================
    // ELIMINAR ORDEN
    // ==========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_ORDER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {

        orderUseCase.delete(id);
        return ApiResponseFactory.success(null, "Orden eliminada correctamente");

    }

}
