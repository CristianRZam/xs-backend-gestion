package com.sistema.sistema.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.sistema.application.dto.request.notification.NotificationCreateRequest;
import com.sistema.sistema.domain.usecase.NotificationUseCase;
import com.sistema.sistema.infrastructure.persistence.cash.CashSessionEntity;
import com.sistema.sistema.infrastructure.persistence.cash.JpaCashSessionRepository;
import com.sistema.sistema.infrastructure.persistence.inventorycount.InventoryCountSessionEntity;
import com.sistema.sistema.infrastructure.persistence.inventorycount.JpaInventoryCountSessionRepository;
import com.sistema.sistema.infrastructure.persistence.product.JpaProductRepository;
import com.sistema.sistema.infrastructure.persistence.product.ProductEntity;
import com.sistema.sistema.infrastructure.persistence.user.JpaUserRepository;
import com.sistema.sistema.infrastructure.persistence.user.UserEntity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperationalAlertServiceTest {

    private final JpaProductRepository products = mock(JpaProductRepository.class);
    private final JpaCashSessionRepository cashSessions = mock(JpaCashSessionRepository.class);
    private final JpaInventoryCountSessionRepository inventoryCounts = mock(JpaInventoryCountSessionRepository.class);
    private final JpaUserRepository users = mock(JpaUserRepository.class);
    private final NotificationUseCase notifications = mock(NotificationUseCase.class);
    private final OperationalAlertService service = new OperationalAlertService(
            products,
            cashSessions,
            inventoryCounts,
            users,
            notifications,
            new ObjectMapper()
    );

    @Test
    void evaluatesAndAssignsAllOperationalAlertsToActiveSuperAdmins() {
        ReflectionTestUtils.setField(service, "lowStockThreshold", 5L);
        ReflectionTestUtils.setField(service, "noMovementDays", 30L);
        ReflectionTestUtils.setField(service, "openCashHours", 12L);
        ReflectionTestUtils.setField(service, "pendingCountHours", 12L);
        when(users.findActiveSuperAdmins()).thenReturn(List.of(UserEntity.builder().id(9L).build()));
        when(products.findByActiveTrueAndDeletedAtIsNullAndTotalStockLessThanEqualOrderByNameAsc(5L))
                .thenReturn(List.of(ProductEntity.builder().id(1L).name("Café").code("CAF-01").totalStock(2L).build()));
        when(products.findActiveProductsWithoutMovementSince(any(), any()))
                .thenReturn(List.of(ProductEntity.builder().id(2L).name("Té").code("TE-01").build()));
        when(cashSessions.findByStatusAndDeletedAtIsNullAndOpenedAtBefore(any(), any()))
                .thenReturn(List.of(CashSessionEntity.builder().id(3L).cashRegisterId(1L)
                        .openedAt(LocalDateTime.now().minusHours(13)).build()));
        when(inventoryCounts.findByStatusInAndOpenedAtBefore(anyList(), any()))
                .thenReturn(List.of(InventoryCountSessionEntity.builder().id(4L).countNumber("CNT-20260922080000")
                        .status("REVIEW").openedAt(LocalDateTime.now().minusHours(13)).build()));

        service.evaluate();

        ArgumentCaptor<NotificationCreateRequest> requests = ArgumentCaptor.forClass(NotificationCreateRequest.class);
        verify(notifications, times(4)).create(requests.capture(), any());
        verify(users).findActiveSuperAdmins();
        assertEquals(List.of("LOW_STOCK", "NO_MOVEMENT", "CASH_SESSION_OPEN", "INVENTORY_COUNT_PENDING"),
                requests.getAllValues().stream().map(NotificationCreateRequest::getType).toList());
        assertTrue(requests.getAllValues().stream()
                .allMatch(request -> request.getEventKey().contains(":DATE:")));
    }
}
