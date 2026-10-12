package com.sistema.sistema.application.service;

import com.sistema.sistema.domain.model.Order;
import com.sistema.sistema.domain.model.OrderItem;
import com.sistema.sistema.domain.repository.OrderRepository;
import com.sistema.sistema.domain.repository.ProductRepository;
import com.sistema.sistema.domain.repository.SaleRepository;
import com.sistema.sistema.domain.repository.OrderItemIncidentRepository;
import com.sistema.sistema.domain.usecase.CashSessionUseCase;
import com.sistema.sistema.domain.usecase.InventoryMovementUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private final OrderRepository repository = mock(OrderRepository.class);
    private final CashSessionUseCase cashSessionUseCase = mock(CashSessionUseCase.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final SaleRepository saleRepository = mock(SaleRepository.class);
    private final InventoryMovementUseCase inventoryMovementUseCase = mock(InventoryMovementUseCase.class);
    private final OrderItemIncidentRepository incidentRepository = mock(OrderItemIncidentRepository.class);
    private final OrderService service = new OrderService(
            repository,
            cashSessionUseCase,
            productRepository,
            saleRepository,
            inventoryMovementUseCase,
            incidentRepository
    );

    @Test
    void updateRejectsOrdersWithNonPendingItems() {
        for (String status : List.of("PREPARING", "READY")) {
            Order current = order(status, 4L);
            Order requested = order(null, null);
            when(repository.getById(10L)).thenReturn(current);
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> service.update(10L, requested)
            );

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            verifyNoInteractions(productRepository);
            reset(repository, productRepository);
        }
    }

    @Test
    void updateRejectsCompletedAndCancelledOrders() {
        for (String status : List.of("COMPLETED", "CANCELLED")) {
            when(repository.getById(10L)).thenReturn(order(status, 4L));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> service.update(10L, order(null, null))
            );

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
            verifyNoInteractions(productRepository);
            reset(repository, productRepository);
        }
    }

    @Test
    void addItemReservesStockAndCreatesANewPendingLine() {
        Order current = order("PENDING", 4L);
        OrderItem item = item(2L, BigDecimal.valueOf(2), "PENDING");
        when(repository.getById(10L)).thenReturn(current);
        when(productRepository.reserveStock(2L, 2L)).thenReturn(true);
        when(repository.addItem(10L, item)).thenReturn(current);

        Order result = service.addItem(10L, item);

        assertEquals(current, result);
        assertEquals("PENDING", item.getStatus());
        assertEquals("DINE_IN", item.getFulfillmentType());
        verify(productRepository).reserveStock(2L, 2L);
        verify(repository).addItem(10L, item);
    }

    @Test
    void updatePendingItemAdjustsReservationsWhenProductAndQuantityChange() {
        OrderItem existing = item(1L, BigDecimal.ONE, "PENDING");
        existing.setId(100L);
        Order current = order("PENDING", 4L, existing);
        OrderItem requested = item(2L, BigDecimal.valueOf(2), "PENDING");
        when(repository.getById(10L)).thenReturn(current);
        when(productRepository.reserveStock(2L, 2L)).thenReturn(true);
        when(repository.updatePendingItem(eq(10L), eq(100L), eq(requested), isNull())).thenReturn(current);

        Order result = service.updatePendingItem(10L, 100L, requested);

        assertEquals(current, result);
        verify(productRepository).reserveStock(2L, 2L);
        verify(productRepository).releaseReservedStock(1L, 1L);
        verify(repository).updatePendingItem(eq(10L), eq(100L), eq(requested), isNull());
    }

    @Test
    void cancelPendingItemReleasesItsReservation() {
        OrderItem existing = item(1L, BigDecimal.valueOf(3), "PENDING");
        existing.setId(100L);
        Order current = order("PENDING", 4L, existing);
        when(repository.getById(10L)).thenReturn(current);
        when(repository.cancelServedItem(eq(10L), eq(100L), eq("[RETURN_TO_STOCK] Sin disponibilidad"), isNull())).thenReturn(current);

        Order result = service.cancelPendingItem(10L, 100L, "Sin disponibilidad", "RETURN_TO_STOCK");

        assertEquals(current, result);
        verify(productRepository).releaseReservedStock(1L, 3L);
        verify(repository).cancelServedItem(eq(10L), eq(100L), eq("[RETURN_TO_STOCK] Sin disponibilidad"), isNull());
    }

    @Test
    void splitPendingItemKeepsTheExistingReservation() {
        OrderItem existing = item(1L, BigDecimal.valueOf(3), "PENDING");
        existing.setId(100L);
        Order current = order("PENDING", 4L, existing);
        when(repository.getById(10L)).thenReturn(current);
        when(repository.splitPendingItem(eq(10L), eq(100L), eq(BigDecimal.ONE), isNull())).thenReturn(current);

        Order result = service.splitPendingItem(10L, 100L, BigDecimal.ONE);

        assertEquals(current, result);
        verify(repository).splitPendingItem(eq(10L), eq(100L), eq(BigDecimal.ONE), isNull());
        verifyNoInteractions(productRepository);
    }

    private Order order(String status, Long cashRegisterId) {
        return order(status, cashRegisterId, item(1L, BigDecimal.ONE, "PENDING"));
    }

    private Order order(String status, Long cashRegisterId, OrderItem item) {
        return Order.builder()
                .orderNumber("ORD-TEST")
                .cashRegisterId(cashRegisterId)
                .orderType("DINE_IN")
                .status(status)
                .items(List.of(item))
                .build();
    }

    private OrderItem item(Long productId, BigDecimal quantity, String status) {
        return OrderItem.builder()
                .productId(productId)
                .quantity(quantity)
                .unitPrice(BigDecimal.TEN)
                .status(status)
                .build();
    }
}
