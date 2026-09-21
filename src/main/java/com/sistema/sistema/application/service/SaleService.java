package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.inventorymovement.InventoryMovementCreateRequest;
import com.sistema.sistema.application.dto.request.sale.SaleCreateRequest;
import com.sistema.sistema.application.dto.response.product.ProductDTO;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;
import com.sistema.sistema.domain.model.CashSession;
import com.sistema.sistema.domain.model.Order;
import com.sistema.sistema.domain.model.OrderItem;
import com.sistema.sistema.domain.repository.IventoryMovementRepository;
import com.sistema.sistema.domain.repository.OrderRepository;
import com.sistema.sistema.domain.repository.ProductRepository;
import com.sistema.sistema.domain.repository.SaleRepository;
import com.sistema.sistema.domain.usecase.CashSessionUseCase;
import com.sistema.sistema.domain.usecase.SaleUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sistema.sistema.application.dto.response.sale.CashSessionSalesSummaryDTO;

@Service
public class SaleService implements SaleUseCase {
    private final SaleRepository saleRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CashSessionUseCase cashSessionUseCase;
    private final IventoryMovementRepository inventoryRepository;

    public SaleService(SaleRepository saleRepository, OrderRepository orderRepository,
                       ProductRepository productRepository, CashSessionUseCase cashSessionUseCase,
                       IventoryMovementRepository inventoryRepository) {
        this.saleRepository = saleRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cashSessionUseCase = cashSessionUseCase;
        this.inventoryRepository = inventoryRepository;
    }

    @Override @Transactional
    public SaleDTO create(SaleCreateRequest request) {
        if (!cashSessionUseCase.existsOpenSession()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Debe abrir una sesión de caja antes de registrar una venta.");
        }
        CashSession session = cashSessionUseCase.getCurrentSession();
        validatePayments(request.getPayments(), calculateTotal(request));

        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.getById(request.getOrderId());
            if (saleRepository.existsByOrderId(order.getId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "La orden ya tiene una venta registrada.");
            }
            if ("CANCELLED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "La orden no puede convertirse en venta con su estado actual.");
            }
            validateMatchesOrder(order, request.getItems());
        }

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
        if (order != null) orderRepository.updateStatus(order.getId(), "COMPLETED");
        return sale;
    }

    @Override public SaleDTO getById(Long id) { return saleRepository.getById(id); }
    @Override public List<SaleDTO> getAll() { return saleRepository.getAll(); }
    @Override public CashSessionSalesSummaryDTO getCashSessionSummary(Long cashSessionId) {
        if (cashSessionId == null) throw new BusinessException(HttpStatus.BAD_REQUEST, "La sesión de caja es obligatoria.");
        return saleRepository.getCashSessionSummary(cashSessionId);
    }

    private BigDecimal calculateTotal(SaleCreateRequest request) {
        BigDecimal itemTotal = request.getItems().stream().map(item -> item.getUnitPrice()
                .multiply(item.getQuantity()).subtract(zero(item.getDiscount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = itemTotal.subtract(zero(request.getDiscount()));
        if (total.signum() < 0) throw new BusinessException(HttpStatus.BAD_REQUEST, "El descuento no puede superar el subtotal.");
        return total;
    }
    private void validatePayments(List<SaleCreateRequest.PaymentRequest> payments, BigDecimal total) {
        BigDecimal paid = payments.stream().map(SaleCreateRequest.PaymentRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (paid.compareTo(total) != 0) throw new BusinessException(HttpStatus.BAD_REQUEST, "La suma de pagos debe coincidir con el total de la venta.");
    }
    private void validateMatchesOrder(Order order, List<SaleCreateRequest.SaleItemRequest> items) {
        Map<Long, Long> requested = quantitiesByProduct(items);
        Map<Long, Long> ordered = new HashMap<>();
        for (OrderItem item : order.getItems()) ordered.merge(item.getProductId(), exactQuantity(item.getQuantity()), Long::sum);
        if (!requested.equals(ordered)) throw new BusinessException(HttpStatus.BAD_REQUEST, "Los productos y cantidades de la venta deben coincidir con la orden.");
    }
    private Map<Long, Long> quantitiesByProduct(List<SaleCreateRequest.SaleItemRequest> items) {
        Map<Long, Long> result = new HashMap<>();
        for (SaleCreateRequest.SaleItemRequest item : items) result.merge(item.getProductId(), exactQuantity(item.getQuantity()), Long::sum);
        return result;
    }
    private long exactQuantity(BigDecimal quantity) {
        try { return quantity.longValueExact(); }
        catch (ArithmeticException exception) { throw new BusinessException(HttpStatus.BAD_REQUEST, "La cantidad debe ser un número entero."); }
    }
    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
}
