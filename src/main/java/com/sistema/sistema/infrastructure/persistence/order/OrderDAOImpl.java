package com.sistema.sistema.infrastructure.persistence.order;

import com.sistema.sistema.domain.model.Order;
import com.sistema.sistema.domain.repository.OrderRepository;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OrderDAOImpl implements OrderRepository {

    private final JpaOrderRepository jpa;
    private final OrderMapper mapper;

    public OrderDAOImpl(
            JpaOrderRepository jpa,
            OrderMapper mapper
    ) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    // ==========================================================
    // CREAR ORDEN
    // ==========================================================

    @Override
    public Order create(Order order) {

        Long currentUserId = SecurityUtil.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        OrderEntity entity = mapper.toEntity(order);

        entity.setId(null);

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

            });
        }

        OrderEntity saved = jpa.save(entity);

        return mapper.toDomain(saved);
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
                jpa.findByDeletedAtIsNullOrderByIdDesc();

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

        entity.setOrderNumber(order.getOrderNumber());
        entity.setCashRegisterId(order.getCashRegisterId());
        entity.setOrderType(order.getOrderType());
        entity.setTableNumber(order.getTableNumber());
        entity.setNotes(order.getNotes());

        /*
         * Reemplazamos los items actuales por los nuevos.
         */
        entity.getItems().clear();

        if (order.getItems() != null) {

            order.getItems().forEach(item -> {

                OrderItemEntity itemEntity =
                        OrderItemEntity.builder()
                                .id(null)
                                .order(entity)
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .notes(item.getNotes())
                                .createdAt(now)
                                .build();

                entity.getItems().add(itemEntity);
            });
        }

        entity.setModifiedBy(currentUserId);
        entity.setModifiedAt(now);

        OrderEntity saved = jpa.save(entity);

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

        entity.setStatus(status);
        entity.setModifiedBy(currentUserId);
        entity.setModifiedAt(LocalDateTime.now());

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
}