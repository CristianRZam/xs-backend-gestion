package com.sistema.sistema.application.service;

import com.sistema.sistema.domain.model.Order;
import com.sistema.sistema.domain.model.OrderItem;
import com.sistema.sistema.domain.repository.OrderRepository;
import com.sistema.sistema.domain.repository.ProductRepository;
import com.sistema.sistema.domain.usecase.CashSessionUseCase;
import com.sistema.sistema.domain.usecase.OrderUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OrderService implements OrderUseCase {

    private final OrderRepository repository;
    private final CashSessionUseCase cashSessionUseCase;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository repository,
            CashSessionUseCase cashSessionUseCase,
            ProductRepository productRepository
    ) {
        this.repository = repository;
        this.cashSessionUseCase = cashSessionUseCase;
        this.productRepository = productRepository;
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

        return repository.create(order);
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

        return repository.getById(id);
    }

    // ==========================================================
    // OBTENER TODAS
    // ==========================================================

    @Override
    public List<Order> getAll() {

        return repository.getAll();
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

        /*
         * Una orden solamente puede modificarse
         * mientras esté pendiente.
         */
        if (!"PENDING".equals(current.getStatus())) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "La orden solamente puede modificarse mientras se encuentre en estado PENDING."
            );
        }

        adjustReservedStock(current.getItems(), order.getItems());

        // La caja de una orden ya creada no puede ser cambiada por el cliente.
        order.setCashRegisterId(current.getCashRegisterId());
        order.setStatus(current.getStatus());

        return repository.update(id, order);
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
            releaseItems(current.getItems());
        }

        return repository.updateStatus(id, status);
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

        for (Map.Entry<Long, Long> entry : quantitiesByProduct(items).entrySet()) {
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

            if (item.getProductId() == null) {

                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "El producto es obligatorio en cada item."
                );
            }

            if (item.getQuantity() == null ||
                    item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {

                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "La cantidad debe ser mayor que cero."
                );
            }

            if (item.getUnitPrice() == null ||
                    item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {

                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "El precio unitario no puede ser negativo."
                );
            }
        }
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
