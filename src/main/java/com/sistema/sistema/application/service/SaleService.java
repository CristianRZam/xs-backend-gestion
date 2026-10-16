package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.inventorymovement.InventoryMovementCreateRequest;
import com.sistema.sistema.application.dto.request.sale.SaleCreateRequest;
import com.sistema.sistema.application.dto.request.sale.SaleCancellationRequest;
import com.sistema.sistema.application.dto.response.product.ProductDTO;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;
import com.sistema.sistema.application.dto.response.sale.SaleListItemDTO;
import com.sistema.sistema.domain.model.CashSession;
import com.sistema.sistema.domain.model.Order;
import com.sistema.sistema.domain.model.OrderItem;
import com.sistema.sistema.domain.repository.IventoryMovementRepository;
import com.sistema.sistema.domain.repository.OrderRepository;
import com.sistema.sistema.domain.repository.ProductRepository;
import com.sistema.sistema.domain.repository.SaleRepository;
import com.sistema.sistema.domain.repository.OrderItemIncidentRepository;
import com.sistema.sistema.domain.model.OrderItemIncident;
import com.sistema.sistema.domain.usecase.CashSessionUseCase;
import com.sistema.sistema.domain.usecase.SaleUseCase;
import com.sistema.sistema.domain.usecase.NotificationUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import com.sistema.sistema.application.dto.response.PageResponseDTO;
import com.sistema.sistema.application.dto.response.sale.CashSessionSalesSummaryDTO;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SaleService implements SaleUseCase {
    private final SaleRepository saleRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CashSessionUseCase cashSessionUseCase;
    private final IventoryMovementRepository inventoryRepository;
    private final NotificationUseCase notificationUseCase;
    private final OrderItemIncidentRepository incidentRepository;

    public SaleService(SaleRepository saleRepository, OrderRepository orderRepository,
                       ProductRepository productRepository, CashSessionUseCase cashSessionUseCase,
                       IventoryMovementRepository inventoryRepository,
                       NotificationUseCase notificationUseCase,
                       OrderItemIncidentRepository incidentRepository) {
        this.saleRepository = saleRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cashSessionUseCase = cashSessionUseCase;
        this.inventoryRepository = inventoryRepository;
        this.notificationUseCase = notificationUseCase;
        this.incidentRepository = incidentRepository;
    }

    @Override @Transactional
    public SaleDTO create(SaleCreateRequest request) {
        if (!cashSessionUseCase.existsOpenSession()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Debe abrir una sesión de caja antes de registrar una venta.");
        }
        CashSession session = cashSessionUseCase.getCurrentSession();
        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.getById(request.getOrderId());
            if (saleRepository.existsByOrderId(order.getId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "La orden ya tiene una venta registrada.");
            }
            if ("CANCELLED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "La orden no puede convertirse en venta con su estado actual.");
            }
            validateOrderIsReadyToCharge(order);
            validateMatchesOrder(order, request.getItems());
            if (request.getItems().stream().anyMatch(item -> zero(item.getDiscount()).signum() != 0)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Los descuentos por producto no están permitidos para una orden.");
            }
            Order orderForDiscount = order;
            BigDecimal approvedDiscount = incidentRepository.findByOrderId(order.getId()).stream()
                    .filter(incident -> "DISCOUNT".equals(incident.getResolution())
                            && isApprovedIncident(incident.getStatus())
                            && isChargeableOrderItem(orderForDiscount, incident.getOrderItemId()))
                    .map(OrderItemIncident::getDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            request.setDiscount(zero(request.getDiscount()).add(approvedDiscount));
        }
        BigDecimal packagingCharge = request.getOrderId() == null
                ? zero(request.getPackagingCharge())
                : request.getPackagingCharge() == null ? zero(order.getPackagingCharge()) : request.getPackagingCharge();
        BigDecimal deliveryCharge = request.getOrderId() == null
                ? zero(request.getDeliveryCharge())
                : request.getDeliveryCharge() == null ? zero(order.getDeliveryCharge()) : request.getDeliveryCharge();
        validateCharges(order, packagingCharge, deliveryCharge);
        request.setPackagingCharge(packagingCharge);
        request.setDeliveryCharge(deliveryCharge);
        validatePayments(request.getPayments(), calculateTotal(request));

        BigDecimal subtotal = request.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(i.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        SaleDTO sale = saleRepository.create(request, session.getCashRegisterId(), session.getId(), subtotal, calculateTotal(request));

        Map<Long, Long> quantities = quantitiesByProduct(request.getItems());
        for (Map.Entry<Long, Long> entry : quantities.entrySet()) {
            ProductDTO product = productRepository.findById(entry.getKey());
            if (product == null) throw new BusinessException(HttpStatus.NOT_FOUND, "Producto no encontrado.");
            boolean consumed = order == null
                    ? productRepository.consumeAvailableStock(entry.getKey(), entry.getValue())
                    : productRepository.consumeReservedStock(entry.getKey(), entry.getValue());
            if (!consumed) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "No hay disponibilidad suficiente para el producto con id " + entry.getKey() + ".");
            }
            InventoryMovementCreateRequest movement = new InventoryMovementCreateRequest();
            movement.setProductId(entry.getKey()); movement.setType("SALE");
            movement.setQuantity(BigDecimal.valueOf(entry.getValue()));
            movement.setPreviousStock(BigDecimal.valueOf(product.getTotalStock()));
            movement.setCurrentStock(BigDecimal.valueOf(product.getTotalStock() - entry.getValue()));
            movement.setReason("Venta " + sale.getSaleNumber());
            movement.setReferenceType("SALE"); movement.setReferenceId(sale.getId());
            inventoryRepository.create(movement);
        }
        if (order != null) {
            log.info("Completing order {} after sale {}", order.getId(), sale.getId());
            orderRepository.updateCharges(order.getId(), packagingCharge, deliveryCharge,
                    com.sistema.sistema.infrastructure.security.SecurityUtil.getCurrentUserId());
            orderRepository.updateStatus(order.getId(), "COMPLETED");
        }
        notificationUseCase.createDigitalPaymentNotifications(sale);
        return sale;
    }

    @Override public SaleDTO getById(Long id) { return saleRepository.getById(id); }
    @Override public List<SaleDTO> getAll() { return saleRepository.getAll(); }
    @Override public PageResponseDTO<SaleListItemDTO> getPage(int page, int size, LocalDate fromDate, LocalDate toDate, String status, Long productId) { return saleRepository.getPage(Math.max(page, 0), Math.min(Math.max(size, 1), 100), fromDate, toDate, status, productId); }
    @Override public CashSessionSalesSummaryDTO getCashSessionSummary(Long cashSessionId) {
        if (cashSessionId == null) throw new BusinessException(HttpStatus.BAD_REQUEST, "La sesión de caja es obligatoria.");
        return saleRepository.getCashSessionSummary(cashSessionId);
    }

    @Override
    @Transactional
    public SaleDTO cancel(Long id, SaleCancellationRequest request) {
        if (id == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El id de la venta es obligatorio.");
        }
        String reason = request == null || request.getReason() == null ? null : request.getReason().trim();
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El motivo de anulación es obligatorio.");
        }

        SaleDTO sale = saleRepository.getById(id);
        if (!"COMPLETED".equals(sale.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Solo se pueden anular ventas completadas.");
        }
        if (!saleRepository.isOriginalCashSessionOpen(id)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "No se puede anular la venta porque su sesión de caja original ya está cerrada.");
        }

        Order order = sale.getOrderId() == null ? null : orderRepository.getById(sale.getOrderId());
        Map<Long, Long> quantities = quantitiesByProductFromSale(sale);
        for (Map.Entry<Long, Long> entry : quantities.entrySet()) {
            ProductDTO product = productRepository.findById(entry.getKey());
            if (product == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "Producto no encontrado.");
            }
            productRepository.restoreStock(entry.getKey(), entry.getValue());
            InventoryMovementCreateRequest movement = new InventoryMovementCreateRequest();
            movement.setProductId(entry.getKey());
            movement.setType("SALE_RETURN");
            movement.setQuantity(BigDecimal.valueOf(entry.getValue()));
            movement.setPreviousStock(BigDecimal.valueOf(product.getTotalStock()));
            movement.setCurrentStock(BigDecimal.valueOf(product.getTotalStock() + entry.getValue()));
            movement.setReason("Anulación de venta " + sale.getSaleNumber() + ": " + reason);
            movement.setReferenceType("SALE");
            movement.setReferenceId(sale.getId());
            inventoryRepository.create(movement);
        }
        if (order != null) {
            for (Map.Entry<Long, Long> entry : quantities.entrySet()) {
                if (!productRepository.reserveStock(entry.getKey(), entry.getValue())) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST,
                            "No se pudo reservar nuevamente el producto con id " + entry.getKey() + ".");
                }
            }
            orderRepository.updateStatus(order.getId(), "READY");
        }
        return saleRepository.cancel(id, reason);
    }

    private BigDecimal calculateTotal(SaleCreateRequest request) {
        BigDecimal itemTotal = request.getItems().stream().map(item -> item.getUnitPrice()
                .multiply(item.getQuantity()).subtract(zero(item.getDiscount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = itemTotal.subtract(zero(request.getDiscount()))
                .add(zero(request.getPackagingCharge()))
                .add(zero(request.getDeliveryCharge()));
        if (total.signum() < 0) throw new BusinessException(HttpStatus.BAD_REQUEST, "El descuento no puede superar el subtotal.");
        return total;
    }
    private void validatePayments(List<SaleCreateRequest.PaymentRequest> payments, BigDecimal total) {
        BigDecimal paid = payments.stream().map(SaleCreateRequest.PaymentRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (paid.compareTo(total) != 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "La suma de pagos debe coincidir con el total de la venta.");
        }

        long cashPayments = payments.stream()
                .filter(payment -> "CASH".equals(normalizePaymentMethod(payment.getPaymentMethod())))
                .count();
        if (cashPayments > 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Solo se permite un pago en efectivo por venta.");
        }

        for (SaleCreateRequest.PaymentRequest payment : payments) {
            String paymentMethod = normalizePaymentMethod(payment.getPaymentMethod());
            if ("CASH".equals(paymentMethod)) {
                if (payment.getReceivedAmount() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST,
                            "Debe indicar el monto recibido para el pago en efectivo.");
                }
                if (payment.getReceivedAmount().compareTo(payment.getAmount()) < 0) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST,
                            "El monto recibido en efectivo no puede ser menor al importe aplicado.");
                }
            } else if (payment.getReceivedAmount() != null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "El monto recibido solo se registra para pagos en efectivo.");
            }
        }
    }
    private void validateMatchesOrder(Order order, List<SaleCreateRequest.SaleItemRequest> items) {
        Map<Long, Long> requested = quantitiesByProduct(items);
        Map<Long, Long> ordered = new HashMap<>();
        for (OrderItem item : chargeableItems(order)) ordered.merge(item.getProductId(), exactQuantity(item.getQuantity()), Long::sum);
        if (!requested.equals(ordered)) throw new BusinessException(HttpStatus.BAD_REQUEST, "Los productos y cantidades de la venta deben coincidir con la orden.");

        Map<Long, BigDecimal> requestedAmounts = new HashMap<>();
        for (SaleCreateRequest.SaleItemRequest item : items) {
            requestedAmounts.merge(item.getProductId(), item.getUnitPrice().multiply(item.getQuantity()), BigDecimal::add);
        }
        Map<Long, BigDecimal> orderedAmounts = new HashMap<>();
        for (OrderItem item : chargeableItems(order)) {
            orderedAmounts.merge(item.getProductId(), item.getUnitPrice().multiply(item.getQuantity()), BigDecimal::add);
        }
        boolean pricesMatch = requestedAmounts.size() == orderedAmounts.size()
                && requestedAmounts.entrySet().stream().allMatch(entry -> {
                    BigDecimal orderedAmount = orderedAmounts.get(entry.getKey());
                    return orderedAmount != null && orderedAmount.compareTo(entry.getValue()) == 0;
                });
        if (!pricesMatch) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Los precios de la venta deben coincidir con los de la orden.");
        }
        Map<String, BigDecimal> requestedLines = saleQuantitiesByProductAndPrice(items);
        Map<String, BigDecimal> orderedLines = orderQuantitiesByProductAndPrice(chargeableItems(order));
        if (!sameQuantities(requestedLines, orderedLines)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Los productos, precios y cantidades de la venta deben coincidir con los de la orden.");
        }
    }

    private void validateOrderIsReadyToCharge(Order order) {
        List<OrderItem> activeItems = activeItems(order);
        if (activeItems.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La orden no tiene productos activos para cobrar.");
        }
        boolean hasPendingItems = activeItems.stream().anyMatch(item -> !"SERVED".equals(item.getStatus()));
        if (hasPendingItems) {
            log.warn("Rejected charge for order {} because it has unserved items", order.getId());
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede cobrar la orden hasta que todos sus productos activos estén servidos."
            );
        }
    }

    private void validateCharges(Order order, BigDecimal packagingCharge, BigDecimal deliveryCharge) {
        if (packagingCharge.signum() < 0 || deliveryCharge.signum() < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Los cargos de envase y delivery no pueden ser negativos.");
        }
        // Las ventas directas no tienen una orden que determine su modalidad. El vendedor
        // puede aplicar ambos cargos manualmente; en ventas desde orden se valida la modalidad.
        if (order == null) return;
        List<OrderItem> activeItems = activeItems(order);
        boolean hasPackagingEligibleItem = activeItems.stream().anyMatch(item ->
                "TAKEAWAY".equals(item.getFulfillmentType()) || "DELIVERY".equals(item.getFulfillmentType()));
        boolean hasDeliveryItem = activeItems.stream().anyMatch(item -> "DELIVERY".equals(item.getFulfillmentType()));
        if (packagingCharge.signum() > 0 && !hasPackagingEligibleItem) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "El cargo por envase solo aplica cuando existen productos para llevar o delivery.");
        }
        if (deliveryCharge.signum() > 0 && !hasDeliveryItem) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "El cargo por delivery solo aplica cuando existen productos delivery.");
        }
    }

    private List<OrderItem> activeItems(Order order) {
        return order.getItems().stream()
                .filter(item -> !"CANCELLED".equals(item.getStatus()))
                .toList();
    }
    private List<OrderItem> chargeableItems(Order order) {
        java.util.Set<Long> nonChargeableIds = incidentRepository.findByOrderId(order.getId()).stream()
                .filter(incident -> "CANCEL_NO_CHARGE".equals(incident.getResolution())
                        && isApprovedIncident(incident.getStatus()))
                .map(OrderItemIncident::getOrderItemId)
                .collect(java.util.stream.Collectors.toSet());
        return activeItems(order).stream().filter(item -> !nonChargeableIds.contains(item.getId())).toList();
    }
    private Map<Long, Long> quantitiesByProduct(List<SaleCreateRequest.SaleItemRequest> items) {
        Map<Long, Long> result = new HashMap<>();
        for (SaleCreateRequest.SaleItemRequest item : items) result.merge(item.getProductId(), exactQuantity(item.getQuantity()), Long::sum);
        return result;
    }

    private Map<String, BigDecimal> saleQuantitiesByProductAndPrice(
            List<SaleCreateRequest.SaleItemRequest> items) {
        Map<String, BigDecimal> result = new HashMap<>();
        for (SaleCreateRequest.SaleItemRequest item : items) {
            result.merge(lineKey(item.getProductId(), item.getUnitPrice()),
                    item.getQuantity(), BigDecimal::add);
        }
        return result;
    }

    private Map<String, BigDecimal> orderQuantitiesByProductAndPrice(List<OrderItem> items) {
        Map<String, BigDecimal> result = new HashMap<>();
        for (OrderItem item : items) {
            result.merge(lineKey(item.getProductId(), item.getUnitPrice()),
                    item.getQuantity(), BigDecimal::add);
        }
        return result;
    }

    private boolean sameQuantities(Map<String, BigDecimal> left, Map<String, BigDecimal> right) {
        return left.size() == right.size() && left.entrySet().stream().allMatch(entry -> {
            BigDecimal expected = right.get(entry.getKey());
            return expected != null && entry.getValue().compareTo(expected) == 0;
        });
    }

    private String lineKey(Long productId, BigDecimal unitPrice) {
        return productId + "|" + unitPrice.stripTrailingZeros().toPlainString();
    }
    private Map<Long, Long> quantitiesByProductFromSale(SaleDTO sale) {
        Map<Long, Long> result = new HashMap<>();
        sale.getItems().forEach(item -> result.merge(item.getProductId(), exactQuantity(item.getQuantity()), Long::sum));
        return result;
    }
    private long exactQuantity(BigDecimal quantity) {
        try { return quantity.longValueExact(); }
        catch (ArithmeticException exception) { throw new BusinessException(HttpStatus.BAD_REQUEST, "La cantidad debe ser un número entero."); }
    }
    private boolean isApprovedIncident(String status) {
        return "APPROVED".equalsIgnoreCase(status) || "APROVED".equalsIgnoreCase(status);
    }
    private boolean isChargeableOrderItem(Order order, Long itemId) {
        return order.getItems().stream()
                .anyMatch(item -> itemId.equals(item.getId()) && !"CANCELLED".equals(item.getStatus()));
    }
    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private String normalizePaymentMethod(String paymentMethod) {
        return paymentMethod == null ? "" : paymentMethod.trim().toUpperCase(Locale.ROOT);
    }
}
