package com.sistema.sistema.infrastructure.persistence.order;

import com.sistema.sistema.domain.model.Order;
import com.sistema.sistema.domain.model.OrderItem;
import com.sistema.sistema.domain.model.OrderItemStatusHistory;
import com.sistema.sistema.domain.repository.OrderRepository;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.sistema.sistema.application.dto.response.PageResponseDTO;
import com.sistema.sistema.application.dto.response.order.OrderListItemDTO;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

@Repository
public class OrderDAOImpl implements OrderRepository {
    private static final String DOCUMENT_NUMBER_PREFIX = "ORD-";
    private static final DateTimeFormatter DOCUMENT_NUMBER_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final JpaOrderRepository jpa;
    private final JpaOrderItemRepository itemJpa;
    private final JpaOrderItemStatusHistoryRepository statusHistoryJpa;
    private final OrderMapper mapper;

    public OrderDAOImpl(
            JpaOrderRepository jpa,
            JpaOrderItemRepository itemJpa,
            JpaOrderItemStatusHistoryRepository statusHistoryJpa,
            OrderMapper mapper
    ) {
        this.jpa = jpa;
        this.itemJpa = itemJpa;
        this.statusHistoryJpa = statusHistoryJpa;
        this.mapper = mapper;
    }

    // ==========================================================
    // CREAR ORDEN
    // ==========================================================

    @Override
    public Order create(Order order) {

        Long currentUserId = SecurityUtil.getCurrentUserId();
        LocalDateTime now = nextOrderTime(LocalDateTime.now().withNano(0));

        OrderEntity entity = mapper.toEntity(order);

        entity.setId(null);
        entity.setOrderNumber(formatOrderNumber(now));

        entity.setCreatedBy(currentUserId);
        entity.setCreatedAt(now);

        entity.setModifiedBy(null);
        entity.setModifiedAt(null);

        entity.setDeletedBy(null);
        entity.setDeletedAt(null);

        if (entity.getItems() != null) {

            entity.getItems().forEach(item -> {

                item.setId(null);
                item.setOrder(entity);
                item.setCreatedAt(now);
                item.setModifiedBy(null);
                item.setModifiedAt(null);

            });
        }

        // La nueva lÃ­nea se inserta por cascada; forzamos el flush para que
        // PostgreSQL asigne su id antes de crear el registro de historial.
        OrderEntity saved = jpa.saveAndFlush(entity);
        saveInitialStatusHistory(saved, currentUserId, now);

        return mapper.toDomain(saved);
    }

    @Override
    public Order addItem(Long orderId, OrderItem item) {
        OrderEntity entity = jpa.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + orderId));
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Long currentUserId = SecurityUtil.getCurrentUserId();
        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .order(entity)
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .notes(item.getNotes())
                .fulfillmentType(defaultFulfillmentType(item.getFulfillmentType(), entity.getOrderType()))
                .status(defaultItemStatus(item.getStatus()))
                .createdAt(now)
                .build();
        // Persistimos explícitamente el item para tener su identificador antes
        // de crear el historial inicial.
        OrderItemEntity persistedItem = itemJpa.saveAndFlush(itemEntity);
        entity.getItems().add(persistedItem);
        entity.setModifiedBy(currentUserId);
        entity.setModifiedAt(now);
        entity.setStatus(deriveOrderStatus(entity.getItems()));
        // El item dividido tambiÃ©n recibe el id durante el flush en cascada.
        OrderEntity saved = jpa.saveAndFlush(entity);
        statusHistoryJpa.save(OrderItemStatusHistoryEntity.builder()
                .orderItemId(persistedItem.getId())
                .newStatus(persistedItem.getStatus())
                .changedBy(currentUserId)
                .changedAt(now)
                .build());
        return mapper.toDomain(saved);
    }

    @Override
    public Order updatePendingItem(Long orderId, Long itemId, OrderItem item, Long modifiedBy) {
        OrderEntity entity = findOrderEntity(orderId);
        OrderItemEntity itemEntity = findItemEntity(entity, itemId);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        itemEntity.setProductId(item.getProductId());
        itemEntity.setQuantity(item.getQuantity());
        itemEntity.setUnitPrice(item.getUnitPrice());
        itemEntity.setNotes(item.getNotes());
        itemEntity.setFulfillmentType(defaultFulfillmentType(item.getFulfillmentType(), entity.getOrderType()));
        itemEntity.setModifiedBy(modifiedBy);
        itemEntity.setModifiedAt(now);
        entity.setModifiedBy(modifiedBy);
        entity.setModifiedAt(now);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Order cancelPendingItem(Long orderId, Long itemId, String comment, Long changedBy) {
        OrderEntity entity = findOrderEntity(orderId);
        OrderItemEntity item = findItemEntity(entity, itemId);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        item.setStatus("CANCELLED");
        item.setModifiedBy(changedBy);
        item.setModifiedAt(now);
        entity.setStatus(deriveOrderStatus(entity.getItems()));
        entity.setModifiedBy(changedBy);
        entity.setModifiedAt(now);
        OrderEntity saved = jpa.save(entity);
        saveStatusHistory(itemId, "PENDING", "CANCELLED", comment, changedBy, now);
        return mapper.toDomain(saved);
    }

    @Override
    public Order splitPendingItem(Long orderId, Long itemId, java.math.BigDecimal quantity, Long changedBy) {
        OrderEntity entity = findOrderEntity(orderId);
        OrderItemEntity source = findItemEntity(entity, itemId);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        source.setQuantity(source.getQuantity().subtract(quantity));
        source.setModifiedBy(changedBy);
        source.setModifiedAt(now);
        OrderItemEntity split = OrderItemEntity.builder()
                .order(entity)
                .productId(source.getProductId())
                .quantity(quantity)
                .unitPrice(source.getUnitPrice())
                .notes(source.getNotes())
                .fulfillmentType(source.getFulfillmentType())
                .status("PENDING")
                .createdAt(now)
                .build();
        OrderItemEntity persistedSplit = itemJpa.saveAndFlush(split);
        entity.getItems().add(persistedSplit);
        entity.setModifiedBy(changedBy);
        entity.setModifiedAt(now);
        OrderEntity saved = jpa.saveAndFlush(entity);
        statusHistoryJpa.save(OrderItemStatusHistoryEntity.builder()
                .orderItemId(persistedSplit.getId())
                .newStatus("PENDING")
                .changedBy(changedBy)
                .changedAt(now)
                .comment("Item creado por división parcial")
                .build());
        return mapper.toDomain(saved);
    }

    @Override
    public Order updateItemStatus(Long orderId, Long itemId, String status, String comment, Long changedBy) {
        OrderEntity entity = findOrderEntity(orderId);
        OrderItemEntity item = findItemEntity(entity, itemId);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        String previousStatus = item.getStatus();
        item.setStatus(status);
        item.setModifiedBy(changedBy);
        item.setModifiedAt(now);
        entity.setStatus(deriveOrderStatus(entity.getItems()));
        entity.setModifiedBy(changedBy);
        entity.setModifiedAt(now);
        OrderEntity saved = jpa.save(entity);
        saveStatusHistory(itemId, previousStatus, status, comment, changedBy, now);
        return mapper.toDomain(saved);
    }

    @Override
    public Order cancelServedItem(Long orderId, Long itemId, String comment, Long changedBy) {
        return updateItemStatus(orderId, itemId, "CANCELLED", comment, changedBy);
    }

    @Override
    public List<OrderItemStatusHistory> getItemStatusHistory(Long orderId, Long itemId) {
        OrderEntity entity = jpa.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + orderId));
        boolean itemBelongsToOrder = entity.getItems().stream().anyMatch(item -> itemId.equals(item.getId()));
        if (!itemBelongsToOrder) {
            throw new EntityNotFoundException("Producto de orden no encontrado con id: " + itemId);
        }
        return statusHistoryJpa.findHistoryWithActorByOrderItemId(itemId).stream()
                .map(this::toDomainHistory)
                .toList();
    }

    // ==========================================================
    // OBTENER POR ID
    // ==========================================================

    @Override
    public Order getById(Long id) {

        OrderEntity entity = jpa
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Orden no encontrada con id: " + id
                        )
                );

        return mapper.toDomain(entity);
    }

    // ==========================================================
    // OBTENER TODAS
    // ==========================================================

    @Override
    public List<Order> getAll() {

        List<OrderEntity> entities =
                jpa.findByDeletedAtIsNullOrderByCreatedAtDescIdDesc();

        return mapper.toDomainList(entities);
    }

    // ==========================================================
    // ACTUALIZAR ORDEN
    // ==========================================================

    @Override
    public Order update(
            Long id,
            Order order
    ) {

        OrderEntity entity = jpa
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Orden no encontrada con id: " + id
                        )
                );

        Long currentUserId = SecurityUtil.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        entity.setCashRegisterId(order.getCashRegisterId());
        entity.setOrderType(order.getOrderType());
        entity.setTableNumber(order.getTableNumber());
        entity.setNotes(order.getNotes());

        /*
         * Reemplazamos los items actuales por los nuevos.
         */
        List<OrderItemEntity> newItems = new ArrayList<>();
        List<OrderItem> requestedItems = order.getItems() == null ? List.of() : order.getItems();

        for (OrderItemEntity existing : entity.getItems()) {
            OrderItem requested = requestedItems.stream()
                    .filter(item -> existing.getId().equals(item.getId()))
                    .findFirst()
                    .orElse(null);

            if (requested == null) {
                if (!"CANCELLED".equals(existing.getStatus())) {
                    String previousStatus = existing.getStatus();
                    existing.setStatus("CANCELLED");
                    existing.setModifiedBy(currentUserId);
                    existing.setModifiedAt(now);
                    saveStatusHistory(
                            existing.getId(),
                            previousStatus,
                            "CANCELLED",
                            "Cancelado al editar la orden",
                            currentUserId,
                            now
                    );
                }
                continue;
            }

            existing.setProductId(requested.getProductId());
            existing.setQuantity(requested.getQuantity());
            existing.setUnitPrice(requested.getUnitPrice());
            existing.setNotes(requested.getNotes());
            existing.setFulfillmentType(defaultFulfillmentType(requested.getFulfillmentType(), entity.getOrderType()));
            existing.setModifiedBy(currentUserId);
            existing.setModifiedAt(now);
        }

        for (OrderItem requested : requestedItems) {
            if (requested.getId() != null) continue;
            OrderItemEntity itemEntity = OrderItemEntity.builder()
                    .order(entity)
                    .productId(requested.getProductId())
                    .quantity(requested.getQuantity())
                    .unitPrice(requested.getUnitPrice())
                    .notes(requested.getNotes())
                    .fulfillmentType(defaultFulfillmentType(requested.getFulfillmentType(), entity.getOrderType()))
                    .status("PENDING")
                    .createdAt(now)
                    .build();
            // El historial exige el id del ítem. Al editar una orden, el
            // save de la orden puede hacer merge y dejar esta instancia
            // transitoria sin el id generado; por eso persistimos el ítem
            // explícitamente antes de registrar su estado inicial.
            OrderItemEntity persistedItem = itemJpa.saveAndFlush(itemEntity);
            entity.getItems().add(persistedItem);
            newItems.add(persistedItem);
        }

        entity.setModifiedBy(currentUserId);
        entity.setModifiedAt(now);

        OrderEntity saved = jpa.saveAndFlush(entity);
        newItems.forEach(item -> saveStatusHistory(
                item.getId(), null, "PENDING", "Producto agregado al editar la orden", currentUserId, now
        ));

        return mapper.toDomain(saved);
    }

    // ==========================================================
    // ACTUALIZAR ESTADO
    // ==========================================================

    @Override
    public Order updateStatus(
            Long id,
            String status
    ) {

        OrderEntity entity = jpa
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Orden no encontrada con id: " + id
                        )
                );

        Long currentUserId = SecurityUtil.getCurrentUserId();

        LocalDateTime changedAt = LocalDateTime.now();
        if ("CANCELLED".equals(status)) {
            for (OrderItemEntity item : entity.getItems()) {
                if ("CANCELLED".equals(item.getStatus())) continue;
                String previousStatus = item.getStatus();
                item.setStatus("CANCELLED");
                item.setModifiedBy(currentUserId);
                item.setModifiedAt(changedAt);
                saveStatusHistory(
                        item.getId(),
                        previousStatus,
                        "CANCELLED",
                        "Cancelado junto con la orden",
                        currentUserId,
                        changedAt
                );
            }
        }

        entity.setStatus(status);
        entity.setModifiedBy(currentUserId);
        entity.setModifiedAt(changedAt);

        OrderEntity saved = jpa.save(entity);

        return mapper.toDomain(saved);
    }

    // ==========================================================
    // ELIMINAR ORDEN
    // ==========================================================

    @Override
    public void delete(Long id) {

        OrderEntity entity = jpa
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Orden no encontrada con id: " + id
                        )
                );

        Long currentUserId = SecurityUtil.getCurrentUserId();

        entity.setDeletedBy(currentUserId);
        entity.setDeletedAt(LocalDateTime.now());

        jpa.save(entity);
    }
    @Override
    public PageResponseDTO<OrderListItemDTO> getPage(
            int page,
            int size,
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            String search,
            Long productId
    ) {
        LocalDateTime from = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime to = toDate == null ? null : toDate.plusDays(1).atStartOfDay();
        String requestedStatus = normalize(status);
        String requestedSearch = normalize(search);
        // The native list query owns the stable created_at/id ordering.
        PageRequest pageable = PageRequest.of(page, size);
        Page<OrderListProjection> result = jpa.findListPage(
                from,
                to,
                requestedStatus == null ? null : requestedStatus.toUpperCase(),
                requestedSearch,
                productId,
                pageable
        );
        return new PageResponseDTO<>(
                result.getContent().stream().map(this::toListItem).toList(),
                result.getTotalElements(),
                page,
                size,
                result.hasNext()
        );
    }

    private OrderListItemDTO toListItem(OrderListProjection row) {
        return OrderListItemDTO.builder()
                .id(row.getId())
                .orderNumber(row.getOrderNumber())
                .orderType(row.getOrderType())
                .tableNumber(row.getTableNumber())
                .status(row.getStatus())
                .hasNotes(Boolean.TRUE.equals(row.getHasNotes()))
                .createdAt(row.getCreatedAt())
                .createdByName(row.getCreatedByName())
                .modifiedAt(row.getModifiedAt())
                .activeItemCount(row.getActiveItemCount())
                .activeUnitCount(row.getActiveUnitCount())
                .totalAmount(row.getTotalAmount())
                .pendingItemCount(row.getPendingItemCount())
                .preparingItemCount(row.getPreparingItemCount())
                .readyItemCount(row.getReadyItemCount())
                .servedItemCount(row.getServedItemCount())
                .cancelledItemCount(row.getCancelledItemCount())
                .build();
    }

    @Override
    public PageResponseDTO<Order> getKitchenQueue(int page, int size) {
        Page<OrderEntity> result = jpa.findKitchenQueue(PageRequest.of(page, size));
        return new PageResponseDTO<>(
                mapper.toDomainList(result.getContent()),
                result.getTotalElements(),
                page,
                size,
                result.hasNext()
        );
    }

    private String normalize(String value) {
        return value == null || value.isBlank() || "ALL".equalsIgnoreCase(value)
                ? null
                : value.trim();
    }

    private OrderEntity findOrderEntity(Long orderId) {
        return jpa.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + orderId));
    }

    private OrderItemEntity findItemEntity(OrderEntity order, Long itemId) {
        return order.getItems().stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Producto de orden no encontrado con id: " + itemId));
    }

    private void saveStatusHistory(Long itemId, String previousStatus, String newStatus,
                                   String comment, Long changedBy, LocalDateTime changedAt) {
        statusHistoryJpa.save(OrderItemStatusHistoryEntity.builder()
                .orderItemId(itemId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .comment(comment)
                .changedBy(changedBy)
                .changedAt(changedAt)
                .build());
    }

    private String defaultFulfillmentType(String fulfillmentType, String orderType) {
        if (fulfillmentType != null && !fulfillmentType.isBlank()) {
            return fulfillmentType.trim().toUpperCase();
        }
        return switch (orderType == null ? "DINE_IN" : orderType.trim().toUpperCase()) {
            case "TAKEAWAY" -> "TAKEAWAY";
            case "DELIVERY" -> "DELIVERY";
            default -> "DINE_IN";
        };
    }

    private String defaultItemStatus(String status) {
        return status == null ? "PENDING" : status;
    }

    private void saveInitialStatusHistory(OrderEntity order, Long changedBy, LocalDateTime changedAt) {
        List<OrderItemStatusHistoryEntity> history = order.getItems().stream()
                .map(item -> OrderItemStatusHistoryEntity.builder()
                        .orderItemId(item.getId())
                        .newStatus(item.getStatus())
                        .changedBy(changedBy)
                        .changedAt(changedAt)
                        .build())
                .toList();
        statusHistoryJpa.saveAll(history);
    }

    private String deriveOrderStatus(List<OrderItemEntity> items) {
        List<OrderItemEntity> activeItems = items.stream()
                .filter(item -> !"CANCELLED".equals(item.getStatus()))
                .toList();
        if (activeItems.isEmpty()) return "CANCELLED";
        if (activeItems.stream().allMatch(item -> "PENDING".equals(item.getStatus()))) return "PENDING";
        if (activeItems.stream().allMatch(item -> "READY".equals(item.getStatus()) || "SERVED".equals(item.getStatus()))) return "READY";
        return "PREPARING";
    }

    private OrderItemStatusHistory toDomainHistory(OrderItemStatusHistoryProjection projection) {
        return OrderItemStatusHistory.builder()
                .id(projection.getId())
                .orderItemId(projection.getOrderItemId())
                .previousStatus(projection.getPreviousStatus())
                .newStatus(projection.getNewStatus())
                .comment(projection.getComment())
                .changedBy(projection.getChangedBy())
                .changedByName(projection.getChangedByName())
                .changedAt(projection.getChangedAt())
                .build();
    }

    private LocalDateTime nextOrderTime(LocalDateTime now) {
        LocalDateTime candidateTime = now;
        do {
            String candidate = formatOrderNumber(candidateTime);
            if (!jpa.existsByOrderNumber(candidate)) {
                return candidateTime;
            }
            candidateTime = candidateTime.plusSeconds(1);
        } while (true);
    }

    private String formatOrderNumber(LocalDateTime value) {
        return DOCUMENT_NUMBER_PREFIX + DOCUMENT_NUMBER_FORMAT.format(value);
    }
}
