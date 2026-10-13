package com.sistema.sistema.application.service;

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
import com.sistema.sistema.domain.repository.OrderItemIncidentRepository;
import com.sistema.sistema.domain.usecase.CashSessionUseCase;
import com.sistema.sistema.domain.usecase.NotificationUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SaleServiceTest {

    private final SaleRepository saleRepository = mock(SaleRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final CashSessionUseCase cashSessionUseCase = mock(CashSessionUseCase.class);
    private final IventoryMovementRepository inventoryRepository = mock(IventoryMovementRepository.class);
    private final NotificationUseCase notificationUseCase = mock(NotificationUseCase.class);
    private final OrderItemIncidentRepository incidentRepository = mock(OrderItemIncidentRepository.class);
    private final SaleService service = new SaleService(
            saleRepository,
            orderRepository,
            productRepository,
            cashSessionUseCase,
            inventoryRepository,
            notificationUseCase,
            incidentRepository
    );

    @Test
    void createRejectsSaleWhenTheCashSessionWasClosed() {
        when(cashSessionUseCase.existsOpenSession()).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(orderSaleRequest(null))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(
                saleRepository,
                orderRepository,
                productRepository,
                inventoryRepository,
                notificationUseCase
        );
    }

    @Test
    void createRejectsChargeWhenAnActiveOrderItemIsNotServed() {
        when(cashSessionUseCase.existsOpenSession()).thenReturn(true);
        when(cashSessionUseCase.getCurrentSession()).thenReturn(session());
        when(orderRepository.getById(10L)).thenReturn(Order.builder()
                .id(10L)
                .status("PREPARING")
                .items(List.of(orderItem(1L, BigDecimal.ONE, "READY")))
                .build());
        when(saleRepository.existsByOrderId(10L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(orderSaleRequest(10L))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("No se puede cobrar la orden hasta que todos sus productos activos estén servidos.", exception.getMessage());
        verify(saleRepository, never()).create(any(), any(), any(), any(), any());
        verifyNoInteractions(productRepository, inventoryRepository, notificationUseCase);
    }

    @Test
    void createChargesOnlyActiveItemsAndKeepsCancelledItemsOutOfTheSale() {
        Order order = Order.builder()
                .id(10L)
                .status("READY")
                .items(List.of(
                        orderItem(1L, BigDecimal.ONE, "SERVED"),
                        orderItem(2L, BigDecimal.ONE, "CANCELLED")
                ))
                .build();
        SaleDTO savedSale = SaleDTO.builder().id(99L).saleNumber("VTA-TEST").orderId(10L).build();
        when(cashSessionUseCase.existsOpenSession()).thenReturn(true);
        when(cashSessionUseCase.getCurrentSession()).thenReturn(session());
        when(orderRepository.getById(10L)).thenReturn(order);
        when(saleRepository.existsByOrderId(10L)).thenReturn(false);
        when(saleRepository.create(any(), eq(1L), eq(2L), eq(BigDecimal.TEN), eq(BigDecimal.TEN))).thenReturn(savedSale);
        when(productRepository.findById(1L)).thenReturn(ProductDTO.builder().id(1L).totalStock(5L).build());
        when(productRepository.consumeReservedStock(1L, 1L)).thenReturn(true);

        SaleDTO result = service.create(orderSaleRequest(10L));

        assertSame(savedSale, result);
        verify(productRepository).consumeReservedStock(1L, 1L);
        verify(productRepository, never()).consumeReservedStock(2L, 1L);
        verify(orderRepository).updateStatus(10L, "COMPLETED");
        verify(notificationUseCase).createDigitalPaymentNotifications(savedSale);
    }

    private SaleCreateRequest orderSaleRequest(Long orderId) {
        SaleCreateRequest.SaleItemRequest item = new SaleCreateRequest.SaleItemRequest();
        item.setProductId(1L);
        item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(BigDecimal.TEN);
        item.setDiscount(BigDecimal.ZERO);
        SaleCreateRequest.PaymentRequest payment = new SaleCreateRequest.PaymentRequest();
        payment.setPaymentMethod("CASH");
        payment.setAmount(BigDecimal.TEN);
        payment.setReceivedAmount(BigDecimal.TEN);
        SaleCreateRequest request = new SaleCreateRequest();
        request.setOrderId(orderId);
        request.setNotes("Sin azúcar");
        request.setDiscount(BigDecimal.ZERO);
        request.setItems(List.of(item));
        request.setPayments(List.of(payment));
        return request;
    }

    private OrderItem orderItem(Long productId, BigDecimal quantity, String status) {
        return OrderItem.builder()
                .productId(productId)
                .quantity(quantity)
                .unitPrice(BigDecimal.TEN)
                .status(status)
                .build();
    }

    private CashSession session() {
        return CashSession.builder().id(2L).cashRegisterId(1L).build();
    }
}
