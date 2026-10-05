package com.sistema.sistema.application.service;

import com.sistema.sistema.domain.model.Order;
import com.sistema.sistema.domain.model.OrderItem;
import com.sistema.sistema.domain.repository.OrderRepository;
import com.sistema.sistema.domain.repository.ProductRepository;
import com.sistema.sistema.domain.usecase.CashSessionUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private final OrderRepository repository = mock(OrderRepository.class);
    private final CashSessionUseCase cashSessionUseCase = mock(CashSessionUseCase.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final OrderService service = new OrderService(
            repository,
            cashSessionUseCase,
            productRepository
    );

    @Test
    void updateAllowsPreparingAndReadyOrdersAndPreservesTheirStatus() {
        for (String status : List.of("PREPARING", "READY")) {
            Order current = order(status, 4L);
            Order requested = order(null, null);
            when(repository.getById(10L)).thenReturn(current);
            when(repository.update(10L, requested)).thenReturn(requested);

            Order updated = service.update(10L, requested);

            assertSame(requested, updated);
            assertEquals(status, requested.getStatus());
            assertEquals(4L, requested.getCashRegisterId());
            verify(repository).update(10L, requested);
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

    private Order order(String status, Long cashRegisterId) {
        return Order.builder()
                .orderNumber("ORD-TEST")
                .cashRegisterId(cashRegisterId)
                .orderType("DINE_IN")
                .status(status)
                .items(List.of(OrderItem.builder()
                        .productId(1L)
                        .quantity(BigDecimal.ONE)
                        .unitPrice(BigDecimal.TEN)
                        .build()))
                .build();
    }
}
