package com.sistema.sistema.infrastructure.persistence.order;

import com.sistema.sistema.domain.model.Order;
import com.sistema.sistema.domain.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderMapper {

    // ==========================================================
    // ENTITY -> DOMAIN
    // ==========================================================

    public Order toDomain(OrderEntity entity) {

        if (entity == null) {
            return null;
        }

        List<OrderItem> items = new ArrayList<>();

        if (entity.getItems() != null) {

            items = entity.getItems()
                    .stream()
                    .map(this::toDomainItem)
                    .toList();
        }

        return Order.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .cashRegisterId(entity.getCashRegisterId())
                .orderType(entity.getOrderType())
                .tableNumber(entity.getTableNumber())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .items(items)
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .modifiedBy(entity.getModifiedBy())
                .modifiedAt(entity.getModifiedAt())
                .deletedBy(entity.getDeletedBy())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    // ==========================================================
    // ITEM ENTITY -> DOMAIN
    // ==========================================================

    private OrderItem toDomainItem(OrderItemEntity entity) {

        if (entity == null) {
            return null;
        }

        return OrderItem.builder()
                .id(entity.getId())
                .orderId(
                        entity.getOrder() != null
                                ? entity.getOrder().getId()
                                : null
                )
                .productId(entity.getProductId())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // ==========================================================
    // DOMAIN -> ENTITY
    // ==========================================================

    public OrderEntity toEntity(Order domain) {

        if (domain == null) {
            return null;
        }

        OrderEntity entity = OrderEntity.builder()
                .id(domain.getId())
                .orderNumber(domain.getOrderNumber())
                .cashRegisterId(domain.getCashRegisterId())
                .orderType(domain.getOrderType())
                .tableNumber(domain.getTableNumber())
                .status(domain.getStatus())
                .notes(domain.getNotes())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .modifiedBy(domain.getModifiedBy())
                .modifiedAt(domain.getModifiedAt())
                .deletedBy(domain.getDeletedBy())
                .deletedAt(domain.getDeletedAt())
                .build();

        if (domain.getItems() != null) {

            List<OrderItemEntity> itemEntities =
                    domain.getItems()
                            .stream()
                            .map(item -> toEntityItem(item, entity))
                            .toList();

            entity.setItems(itemEntities);
        }

        return entity;
    }

    // ==========================================================
    // ITEM DOMAIN -> ENTITY
    // ==========================================================

    private OrderItemEntity toEntityItem(
            OrderItem domain,
            OrderEntity order
    ) {

        if (domain == null) {
            return null;
        }

        return OrderItemEntity.builder()
                .id(domain.getId())
                .order(order)
                .productId(domain.getProductId())
                .quantity(domain.getQuantity())
                .unitPrice(domain.getUnitPrice())
                .notes(domain.getNotes())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    // ==========================================================
    // LIST
    // ==========================================================

    public List<Order> toDomainList(
            List<OrderEntity> entities
    ) {

        return entities.stream()
                .map(this::toDomain)
                .toList();
    }

}