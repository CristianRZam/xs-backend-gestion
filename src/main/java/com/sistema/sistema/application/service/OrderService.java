package com.sistema.sistema.application.service;

import com.sistema.sistema.domain.model.Order;
import com.sistema.sistema.domain.model.OrderItem;
import com.sistema.sistema.domain.model.OrderItemStatusHistory;
import com.sistema.sistema.domain.repository.OrderRepository;
import com.sistema.sistema.domain.repository.ProductRepository;
import com.sistema.sistema.domain.repository.SaleRepository;
import com.sistema.sistema.domain.repository.OrderItemIncidentRepository;
import com.sistema.sistema.domain.model.OrderItemIncident;
import com.sistema.sistema.domain.usecase.CashSessionUseCase;
import com.sistema.sistema.domain.usecase.InventoryMovementUseCase;
import com.sistema.sistema.domain.usecase.OrderUseCase;
import com.sistema.sistema.application.dto.request.inventorymovement.InventoryMovementCreateRequest;
import com.sistema.sistema.application.dto.request.order.OrderItemIncidentRequest;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.LocalDate;
import com.sistema.sistema.application.dto.response.PageResponseDTO;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderService implements OrderUseCase {

    private final OrderRepository repository;
    private final CashSessionUseCase cashSessionUseCase;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final InventoryMovementUseCase inventoryMovementUseCase;
    private final OrderItemIncidentRepository incidentRepository;

    public OrderService(
            OrderRepository repository,
            CashSessionUseCase cashSessionUseCase,
            ProductRepository productRepository,
            SaleRepository saleRepository,
            InventoryMovementUseCase inventoryMovementUseCase,
            OrderItemIncidentRepository incidentRepository
    ) {
        this.repository = repository;
        this.cashSessionUseCase = cashSessionUseCase;
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.inventoryMovementUseCase = inventoryMovementUseCase;
        this.incidentRepository = incidentRepository;
    }

    // ==========================================================
    // CREAR ORDEN
    // ==========================================================

    @Override
    @Transactional
    public Order create(Order order) {

        if (!cashSessionUseCase.existsOpenSession()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Debe abrir una sesión de caja antes de crear una orden."
            );
        }

        if (order == null) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "La información de la orden es obligatoria."
            );
        }

        // El identificador se asigna en persistencia desde el reloj del servidor.
        order.setOrderNumber("SERVER_GENERATED");
        validateOrder(order);

        if (order.getOrderType() == null ||
                order.getOrderType().isBlank()) {

            order.setOrderType("DINE_IN");
        }

        /*
         * La caja de la orden siempre se determina en el servidor a partir
         * de la sesión abierta. Nunca se confía en un ID enviado por el cliente.
         */
        order.setCashRegisterId(
                cashSessionUseCase
                        .getCurrentSession()
                        .getCashRegisterId()
        );

        reserveItems(order.getItems());

        order.setStatus("PENDING");

        return withIncidentDiscount(repository.create(order));
    }

    // ==========================================================
    // OBTENER POR ID
    // ==========================================================

    @Override
    public Order getById(Long id) {

        if (id == null) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "El id de la orden es obligatorio."
            );
        }

        return withIncidentDiscount(repository.getById(id));
    }

    // ==========================================================
    // OBTENER TODAS
    // ==========================================================

    @Override
    public List<Order> getAll() {

        return repository.getAll().stream().map(this::withIncidentDiscount).toList();
    }

    // ==========================================================
    // ACTUALIZAR ORDEN
    // ==========================================================

    @Override
    @Transactional
    public Order update(
            Long id,
            Order order
    ) {

        if (id == null) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "El id de la orden es obligatorio."
            );
        }

        validateOrder(order);

        Order current = repository.getById(id);

        if (current.getItems().stream().anyMatch(item -> isTerminalItemStatus(item.getStatus()))) {
            log.warn("Rejected full replacement of order {} because it contains terminal items", id);
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "La orden contiene productos servidos o cancelados. Agregue productos mediante la acción correspondiente."
            );
        }

        /*
         * Una orden solamente puede modificarse
         * mientras esté pendiente.
         */
        if (!"PENDING".equals(current.getStatus()) ||
                current.getItems().stream().anyMatch(item -> !"PENDING".equals(item.getStatus()))) {
            log.warn("Rejected full replacement of order {} because it contains non-pending items", id);

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "La edición general solo está permitida cuando todos los productos están pendientes."
            );
        }

        log.info("Updating active order {} in status {}", id, current.getStatus());
        adjustReservedStock(current.getItems(), order.getItems());

        // La caja de una orden ya creada no puede ser cambiada por el cliente.
        order.setCashRegisterId(current.getCashRegisterId());
        order.setStatus(current.getStatus());

        return withIncidentDiscount(repository.update(id, order));
    }

    @Override
    @Transactional
    public Order addItem(Long orderId, OrderItem item) {
        if (orderId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El id de la orden es obligatorio.");
        }

        validateOrderItem(item);
        Order current = repository.getById(orderId);
        ensureActiveOrder(current);

        item.setId(null);
        item.setOrderId(orderId);
        item.setStatus("PENDING");
        item.setFulfillmentType(defaultFulfillmentType(item.getFulfillmentType(), current.getOrderType()));
        reserveItems(List.of(item));

        log.info("Adding product {} as a new item to order {}", item.getProductId(), orderId);
        return withIncidentDiscount(repository.addItem(orderId, item));
    }

    @Override
    @Transactional
    public Order updatePendingItem(Long orderId, Long itemId, OrderItem item) {
        validateOrderItem(item);
        Order current = requireOrderItem(orderId, itemId, "PENDING");
        OrderItem existing = current.getItems().stream().filter(value -> itemId.equals(value.getId())).findFirst().orElseThrow();
        item.setId(itemId);
        item.setOrderId(orderId);
        item.setStatus("PENDING");
        item.setFulfillmentType(defaultFulfillmentType(item.getFulfillmentType(), current.getOrderType()));
        adjustReservedStock(List.of(existing), List.of(item));
        log.info("Updating pending item {} in order {}", itemId, orderId);
        return withIncidentDiscount(repository.updatePendingItem(orderId, itemId, item, SecurityUtil.getCurrentUserId()));
    }

    @Override
    @Transactional
    public Order cancelPendingItem(Long orderId, Long itemId, String comment, String stockDisposition) {
        if (comment == null || comment.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El motivo de cancelación es obligatorio.");
        }
        Order current = repository.getById(orderId);
        ensureActiveOrder(current);
        OrderItem item = current.getItems().stream().filter(value -> itemId.equals(value.getId())).findFirst().orElseThrow();
        if ("CANCELLED".equals(item.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El producto ya está cancelado.");
        }
        String disposition = stockDisposition == null ? "" : stockDisposition.trim().toUpperCase();
        if (!Set.of("RETURN_TO_STOCK", "WASTE").contains(disposition)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El destino del producto debe ser RETURN_TO_STOCK o WASTE.");
        }
        productRepository.releaseReservedStock(item.getProductId(), toStockQuantity(item.getQuantity()));
        if ("WASTE".equals(disposition)) {
            inventoryMovementUseCase.create(InventoryMovementCreateRequest.builder()
                    .productId(item.getProductId()).type("WASTE").quantity(item.getQuantity())
                    .reason("Cancelación de producto: " + comment.trim())
                    .referenceType("ORDER_ITEM_CANCELLATION").referenceId(itemId).build());
        }
        incidentRepository.supersedeApprovedDiscounts(orderId, itemId);
        log.info("Cancelling {} item {} in order {}", disposition, itemId, orderId);
        return withIncidentDiscount(repository.cancelServedItem(orderId, itemId,
                "[" + disposition + "] " + comment.trim(), SecurityUtil.getCurrentUserId()));
    }

    @Override
    @Transactional
    public Order splitPendingItem(Long orderId, Long itemId, BigDecimal quantity) {
        Order current = requireOrderItem(orderId, itemId, "PENDING");
        OrderItem item = current.getItems().stream().filter(value -> itemId.equals(value.getId())).findFirst().orElseThrow();
        toStockQuantity(quantity);
        if (quantity.compareTo(item.getQuantity()) >= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La cantidad a dividir debe ser menor que la cantidad pendiente.");
        }
        log.info("Splitting pending item {} in order {} by quantity {}", itemId, orderId, quantity);
        return withIncidentDiscount(repository.splitPendingItem(orderId, itemId, quantity, SecurityUtil.getCurrentUserId()));
    }

    @Override
    @Transactional
    public Order updateItemStatus(Long orderId, Long itemId, String status, String comment) {
        if (orderId == null || itemId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La orden y el producto son obligatorios.");
        }
        if (status == null || status.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El estado del producto es obligatorio.");
        }

        Order current = repository.getById(orderId);
        ensureActiveOrder(current);
        OrderItem item = current.getItems().stream()
                .filter(currentItem -> itemId.equals(currentItem.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El producto no pertenece a la orden."));

        String requestedStatus = status.trim().toUpperCase();
        validateItemStatusTransition(item.getStatus(), requestedStatus);
        log.info("Changing item {} of order {} from {} to {}", itemId, orderId, item.getStatus(), requestedStatus);
        return withIncidentDiscount(repository.updateItemStatus(
                orderId,
                itemId,
                requestedStatus,
                normalizeOptional(comment),
                SecurityUtil.getCurrentUserId()
        ));
    }

    @Override
    @Transactional
    public Order updateItemsStatus(Long orderId, List<Long> itemIds, String status, String comment) {
        if (itemIds == null || itemIds.isEmpty()) throw new BusinessException(HttpStatus.BAD_REQUEST, "Debe seleccionar al menos un producto.");
        Order order = repository.getById(orderId);
        ensureActiveOrder(order);
        String requested = status == null ? "" : status.trim().toUpperCase();
        boolean restorePrevious = "RESTORE_PREVIOUS".equals(requested);
        String manualCorrectionTarget = manualCorrectionTarget(requested);
        for (Long itemId : itemIds.stream().distinct().toList()) {
            OrderItem item = order.getItems().stream().filter(value -> itemId.equals(value.getId())).findFirst()
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Un producto no pertenece a la orden."));
            if (restorePrevious) validatePreviousStatusRestoration(orderId, item);
            else if (manualCorrectionTarget != null) validateManualStatusCorrection(item);
            else validateItemStatusTransition(item.getStatus(), requested);
        }
        Order updated = order;
        for (Long itemId : itemIds.stream().distinct().toList()) {
            OrderItem item = updated.getItems().stream().filter(value -> itemId.equals(value.getId())).findFirst().orElseThrow();
            String target = restorePrevious ? previousStatus(orderId, item)
                    : manualCorrectionTarget != null ? manualCorrectionTarget : requested;
            if (target.equals(item.getStatus())) continue;
            updated = repository.updateItemStatus(orderId, itemId, target,
                    restorePrevious ? "Corrección: restaurado al estado anterior"
                            : manualCorrectionTarget != null ? manualCorrectionComment(comment) : normalizeOptional(comment),
                    SecurityUtil.getCurrentUserId());
        }
        log.info("Updated {} items of order {} to {}", itemIds.size(), orderId, requested);
        return withIncidentDiscount(updated);
    }

    private void validatePreviousStatusRestoration(Long orderId, OrderItem item) {
        if (!SecurityUtil.hasAuthority("CORRECT_ORDER_ITEM_STATUS")) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No tiene permiso para corregir el estado de un producto.");
        }
        if ("CANCELLED".equals(item.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede corregir un producto cancelado.");
        }
        previousStatus(orderId, item);
    }

    private String previousStatus(Long orderId, OrderItem item) {
        List<OrderItemStatusHistory> history = repository.getItemStatusHistory(orderId, item.getId());
        if (history.isEmpty()) throw new BusinessException(HttpStatus.BAD_REQUEST, "El producto no tiene un estado anterior para restaurar.");
        OrderItemStatusHistory last = history.get(history.size() - 1);
        if (!item.getStatus().equals(last.getNewStatus()) || last.getPreviousStatus() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede restaurar el estado anterior del producto.");
        }
        return last.getPreviousStatus();
    }

    private String manualCorrectionTarget(String requested) {
        if (!requested.startsWith("CORRECT_TO_")) return null;
        String target = requested.substring("CORRECT_TO_".length());
        if (!List.of("PENDING", "PREPARING", "READY", "SERVED").contains(target)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El estado de corrección no es válido.");
        }
        return target;
    }

    private void validateManualStatusCorrection(OrderItem item) {
        if (!SecurityUtil.hasAuthority("CORRECT_ORDER_ITEM_STATUS")) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No tiene permiso para corregir el estado de un producto.");
        }
        if ("CANCELLED".equals(item.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "No se puede corregir un producto cancelado.");
        }
    }

    private String manualCorrectionComment(String comment) {
        String normalized = normalizeOptional(comment);
        return normalized == null ? "Corrección manual de estado" : normalized;
    }

    @Override
    public List<OrderItemStatusHistory> getItemStatusHistory(Long orderId, Long itemId) {
        if (orderId == null || itemId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La orden y el producto son obligatorios.");
        }
        return repository.getItemStatusHistory(orderId, itemId);
    }

    @Override
    @Transactional
    public Order reportServedItemIncident(Long orderId, Long itemId, OrderItemIncidentRequest request) {
        Order order = repository.getById(orderId);
        ensureActiveOrder(order);
        if (saleRepository.existsByOrderId(orderId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "No se pueden registrar incidencias después de cobrar la orden.");
        }
        OrderItem item = order.getItems().stream().filter(value -> itemId.equals(value.getId())).findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El producto no pertenece a la orden."));
        if (!"SERVED".equals(item.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Solo se puede reportar una incidencia sobre un producto servido.");
        }
        String resolution = request.getResolution().trim().toUpperCase();
        if (!Set.of("CANCEL_NO_CHARGE", "REPLACE", "DISCOUNT").contains(resolution)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La resolución debe ser CANCEL_NO_CHARGE o REPLACE.");
        }
        BigDecimal discount = request.getDiscountAmount() == null ? BigDecimal.ZERO : request.getDiscountAmount();
        if ("DISCOUNT".equals(resolution)) {
            if (discount.signum() <= 0) throw new BusinessException(HttpStatus.BAD_REQUEST, "El descuento debe ser mayor a cero.");
            boolean hasActiveDiscount = incidentRepository.findByOrderId(orderId).stream()
                    .anyMatch(value -> itemId.equals(value.getOrderItemId())
                            && "DISCOUNT".equals(value.getResolution())
                            && isApprovedIncident(value.getStatus()));
            if (hasActiveDiscount) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "El producto ya tiene un descuento vigente. Aplique una compensación final en lugar de otro descuento.");
            }
            BigDecimal chargeable = order.getItems().stream().filter(value -> !"CANCELLED".equals(value.getStatus()))
                    .map(value -> value.getUnitPrice().multiply(value.getQuantity())).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal approved = incidentRepository.findByOrderId(orderId).stream()
                    .filter(value -> "DISCOUNT".equals(value.getResolution()) && isApprovedIncident(value.getStatus()))
                    .map(OrderItemIncident::getDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (approved.add(discount).compareTo(chargeable) > 0) throw new BusinessException(HttpStatus.BAD_REQUEST, "El descuento supera el total cobrable de la orden.");
        } else if (discount.signum() != 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Solo las incidencias de descuento pueden tener monto.");
        }
        if ("CANCEL_NO_CHARGE".equals(resolution) || "REPLACE".equals(resolution)) {
            incidentRepository.supersedeApprovedDiscounts(orderId, itemId);
        }
        incidentRepository.save(OrderItemIncident.builder().orderId(orderId).orderItemId(itemId)
                .productId(item.getProductId()).resolution(resolution).reason(request.getReason().trim())
                .discountAmount(discount).reportedBy(SecurityUtil.getCurrentUserId())
                .reportedAt(java.time.LocalDateTime.now().withNano(0)).status("APPROVED")
                .approvedBy(SecurityUtil.getCurrentUserId()).approvedAt(java.time.LocalDateTime.now().withNano(0)).build());
        if ("DISCOUNT".equals(resolution)) {
            log.info("Discount incident approved for served item {} in order {}", itemId, orderId);
            return withIncidentDiscount(order);
        }
        productRepository.releaseReservedStock(item.getProductId(), toStockQuantity(item.getQuantity()));
        inventoryMovementUseCase.create(InventoryMovementCreateRequest.builder().productId(item.getProductId())
                .type("WASTE").quantity(item.getQuantity()).reason(request.getReason().trim())
                .referenceType("ORDER_INCIDENT").referenceId(itemId).build());
        Order updated = repository.cancelServedItem(orderId, itemId,
                "INCIDENCIA [" + resolution + "]: " + request.getReason().trim(), SecurityUtil.getCurrentUserId());
        if ("REPLACE".equals(resolution)) {
            OrderItem replacement = OrderItem.builder().productId(item.getProductId()).quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice()).notes("Reposición por incidencia")
                    .fulfillmentType(item.getFulfillmentType()).build();
            reserveItems(List.of(replacement));
            updated = repository.addItem(orderId, replacement);
        }
        log.info("Incident {} applied to served item {} in order {}", resolution, itemId, orderId);
        return withIncidentDiscount(updated);
    }

    @Override
    public List<OrderItemIncident> getIncidents(Long orderId) {
        repository.getById(orderId);
        return incidentRepository.findByOrderId(orderId);
    }

    private boolean isApprovedIncident(String status) {
        return "APPROVED".equalsIgnoreCase(status) || "APROVED".equalsIgnoreCase(status);
    }

    private Order withIncidentDiscount(Order order) {
        if (order == null || order.getId() == null) return order;
        BigDecimal discount = incidentRepository.findByOrderId(order.getId()).stream()
                .filter(incident -> "DISCOUNT".equals(incident.getResolution())
                        && isApprovedIncident(incident.getStatus())
                        && isChargeableItem(order, incident.getOrderItemId()))
                .map(OrderItemIncident::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setIncidentDiscount(discount);
        return order;
    }

    private boolean isChargeableItem(Order order, Long itemId) {
        return order.getItems().stream()
                .anyMatch(item -> itemId.equals(item.getId()) && !"CANCELLED".equals(item.getStatus()));
    }

    // ==========================================================
    // ACTUALIZAR ESTADO
    // ==========================================================

    @Override
    @Transactional
    public Order updateStatus(
            Long id,
            String status
    ) {

        if (id == null) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "El id de la orden es obligatorio."
            );
        }

        if (status == null || status.isBlank()) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "El estado de la orden es obligatorio."
            );
        }

        status = status.trim().toUpperCase();

        if ("COMPLETED".equals(status)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Una orden se completa únicamente al registrar su venta."
            );
        }

        Set<String> allowedStatuses = Set.of(
                "PENDING",
                "PREPARING",
                "READY",
                "COMPLETED",
                "CANCELLED"
        );

        if (!allowedStatuses.contains(status)) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Estado de orden no válido. Estados permitidos: "
                            + allowedStatuses
            );
        }

        Order current = repository.getById(id);

        validateStatusTransition(
                current.getStatus(),
                status
        );

        if ("CANCELLED".equals(status)) {
            if (current.getItems().stream().anyMatch(item -> "SERVED".equals(item.getStatus()))) {
                log.warn("Rejected cancellation of order {} because it contains served items", id);
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "La orden contiene productos servidos. Deben gestionarse mediante el flujo de incidencias."
                );
            }
            releaseItems(current.getItems());
        }

        return withIncidentDiscount(repository.updateStatus(id, status));
    }

    // ==========================================================
    // ELIMINAR
    // ==========================================================

    @Override
    @Transactional
    public void delete(Long id) {

        if (id == null) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "El id de la orden es obligatorio."
            );
        }

        Order current = repository.getById(id);

        if (!"PENDING".equals(current.getStatus())) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "La orden solamente puede eliminarse mientras se encuentre en estado PENDING."
            );
        }

        releaseItems(current.getItems());

        repository.delete(id);
    }
    @Override public PageResponseDTO<Order> getPage(int page, int size, LocalDate fromDate, LocalDate toDate, String status, String search) {
        PageResponseDTO<Order> result = repository.getPage(Math.max(page, 0), Math.min(Math.max(size, 1), 100), fromDate, toDate, status, search);
        result.getItems().forEach(this::withIncidentDiscount);
        return result;
    }

    @Override
    public PageResponseDTO<Order> getKitchenQueue(int page, int size) {
        PageResponseDTO<Order> result = repository.getKitchenQueue(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100)
        );
        result.getItems().forEach(order -> order.getItems().forEach(item -> {
            var product = productRepository.findById(item.getProductId());
            if (product != null) {
                item.setProductName(product.getName());
            }
        }));
        result.getItems().forEach(this::withIncidentDiscount);
        return result;
    }

    // ==========================================================
    // RESERVA DE STOCK
    // ==========================================================

    private void reserveItems(List<OrderItem> items) {

        for (Map.Entry<Long, Long> entry : quantitiesByProduct(items).entrySet()) {

            boolean reserved = productRepository.reserveStock(
                    entry.getKey(),
                    entry.getValue()
            );

            if (!reserved) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "No hay stock disponible para el producto con id "
                                + entry.getKey()
                                + "."
                );
            }
        }
    }

    private void releaseItems(List<OrderItem> items) {

        List<OrderItem> activeItems = items.stream()
                .filter(item -> !"CANCELLED".equals(item.getStatus()))
                .toList();
        for (Map.Entry<Long, Long> entry : quantitiesByProduct(activeItems).entrySet()) {
            productRepository.releaseReservedStock(
                    entry.getKey(),
                    entry.getValue()
            );
        }
    }

    private void adjustReservedStock(
            List<OrderItem> currentItems,
            List<OrderItem> requestedItems
    ) {

        Map<Long, Long> currentQuantities = quantitiesByProduct(currentItems);
        Map<Long, Long> requestedQuantities = quantitiesByProduct(requestedItems);

        // Primero reservamos los incrementos. Si alguno falla, la transacción
        // revierte todas las reservas y la orden permanece sin modificaciones.
        for (Map.Entry<Long, Long> entry : requestedQuantities.entrySet()) {
            long currentQuantity = currentQuantities.getOrDefault(entry.getKey(), 0L);
            long increase = entry.getValue() - currentQuantity;

            if (increase > 0 && !productRepository.reserveStock(entry.getKey(), increase)) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "No hay stock disponible para el producto con id "
                                + entry.getKey()
                                + "."
                );
            }
        }

        // Una vez asegurados los incrementos, liberamos cantidades reducidas
        // o productos que se retiraron de la orden.
        for (Map.Entry<Long, Long> entry : currentQuantities.entrySet()) {
            long requestedQuantity = requestedQuantities.getOrDefault(entry.getKey(), 0L);
            long decrease = entry.getValue() - requestedQuantity;

            if (decrease > 0) {
                productRepository.releaseReservedStock(entry.getKey(), decrease);
            }
        }
    }

    private Map<Long, Long> quantitiesByProduct(List<OrderItem> items) {

        Map<Long, Long> quantities = new HashMap<>();

        for (OrderItem item : items) {
            long quantity = toStockQuantity(item.getQuantity());
            quantities.merge(item.getProductId(), quantity, Math::addExact);
        }

        return quantities;
    }

    private long toStockQuantity(BigDecimal quantity) {

        try {
            return quantity.longValueExact();
        } catch (ArithmeticException exception) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad de una orden debe ser un número entero para reservar stock."
            );
        }
    }

    // ==========================================================
    // VALIDAR ORDEN
    // ==========================================================

    private void validateOrder(Order order) {

        if (order == null) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "La información de la orden es obligatoria."
            );
        }

        if (order.getOrderNumber() == null ||
                order.getOrderNumber().isBlank()) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "El número de orden es obligatorio."
            );
        }

        if (order.getOrderNumber().length() > 30) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "El número de orden no puede superar los 30 caracteres."
            );
        }

        if (order.getOrderType() != null &&
                !order.getOrderType().isBlank()) {

            String type =
                    order.getOrderType()
                            .trim()
                            .toUpperCase();

            Set<String> allowedTypes = Set.of(
                    "DINE_IN",
                    "TAKEAWAY",
                    "DELIVERY"
            );

            if (!allowedTypes.contains(type)) {

                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "Tipo de orden no válido. Tipos permitidos: "
                                + allowedTypes
                );
            }

            order.setOrderType(type);
        }

        if (order.getItems() == null ||
                order.getItems().isEmpty()) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "La orden debe contener al menos un producto."
            );
        }

        for (OrderItem item : order.getItems()) {
            validateOrderItem(item);
        }
    }

    private void validateOrderItem(OrderItem item) {
        if (item == null || item.getProductId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El producto es obligatorio en cada item.");
        }
        if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor que cero.");
        }
        toStockQuantity(item.getQuantity());
        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El precio unitario no puede ser negativo.");
        }
    }

    private void ensureActiveOrder(Order order) {
        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La orden no admite cambios en su estado actual.");
        }
    }

    private boolean isTerminalItemStatus(String status) {
        return "SERVED".equals(status) || "CANCELLED".equals(status);
    }

    private String defaultFulfillmentType(String fulfillmentType, String orderType) {
        return fulfillmentType == null || fulfillmentType.isBlank()
                ? "TAKEAWAY".equals(orderType) ? "TAKEAWAY" : "DINE_IN"
                : fulfillmentType.trim().toUpperCase();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private void validateItemStatusTransition(String currentStatus, String requestedStatus) {
        String current = currentStatus == null ? "PENDING" : currentStatus;
        boolean isCorrection = ("PENDING".equals(current) && "SERVED".equals(requestedStatus))
                || (("PREPARING".equals(current) || "READY".equals(current) || "SERVED".equals(current))
                && "PENDING".equals(requestedStatus));
        if (isCorrection && !SecurityUtil.hasAuthority("CORRECT_ORDER_ITEM_STATUS")) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "No tiene permiso para corregir el estado de un producto.");
        }
        boolean valid = switch (current) {
            case "PENDING" -> "PREPARING".equals(requestedStatus) || "SERVED".equals(requestedStatus);
            case "PREPARING" -> "READY".equals(requestedStatus) || "PENDING".equals(requestedStatus);
            case "READY" -> "SERVED".equals(requestedStatus) || "PENDING".equals(requestedStatus);
            case "SERVED" -> "PENDING".equals(requestedStatus);
            default -> false;
        };
        if (!valid) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "No se puede cambiar el producto de " + current + " a " + requestedStatus + ".");
        }
    }

    private Order requireOrderItem(Long orderId, Long itemId, String expectedStatus) {
        if (orderId == null || itemId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La orden y el producto son obligatorios.");
        }
        Order order = repository.getById(orderId);
        ensureActiveOrder(order);
        OrderItem item = order.getItems().stream()
                .filter(value -> itemId.equals(value.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El producto no pertenece a la orden."));
        if (!expectedStatus.equals(item.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Solo se pueden modificar productos en estado " + expectedStatus + ".");
        }
        return order;
    }

    // ==========================================================
    // VALIDAR TRANSICIÓN DE ESTADO
    // ==========================================================

    private void validateStatusTransition(
            String currentStatus,
            String newStatus
    ) {

        if (currentStatus.equals(newStatus)) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "La orden ya se encuentra en estado " + newStatus + "."
            );
        }

        boolean valid = switch (currentStatus) {

            case "PENDING" ->
                    newStatus.equals("PREPARING") ||
                            newStatus.equals("CANCELLED");

            case "PREPARING" ->
                    newStatus.equals("READY") ||
                            newStatus.equals("CANCELLED");

            case "READY" ->
                    newStatus.equals("CANCELLED");

            case "COMPLETED", "CANCELLED" ->
                    false;

            default ->
                    false;
        };

        if (!valid) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede cambiar la orden de "
                            + currentStatus
                            + " a "
                            + newStatus
                            + "."
            );
        }
    }

}
