package com.sistema.sistema.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;
import com.sistema.sistema.infrastructure.persistence.notification.JpaNotificationRepository;
import com.sistema.sistema.infrastructure.persistence.notification.JpaUserNotificationRepository;
import com.sistema.sistema.infrastructure.persistence.notification.NotificationEntity;
import com.sistema.sistema.infrastructure.persistence.notification.UserNotificationEntity;
import com.sistema.sistema.infrastructure.persistence.user.JpaUserRepository;
import com.sistema.sistema.infrastructure.persistence.user.UserEntity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class NotificationServiceTest {

    private final JpaNotificationRepository notifications = mock(JpaNotificationRepository.class);
    private final JpaUserNotificationRepository userNotifications = mock(JpaUserNotificationRepository.class);
    private final JpaUserRepository users = mock(JpaUserRepository.class);
    private final NotificationService service = new NotificationService(
            notifications,
            userNotifications,
            users,
            new ObjectMapper()
    );

    @Test
    void cashPaymentDoesNotCreateOrDistributeNotifications() {
        service.createDigitalPaymentNotifications(sale(payment("CASH", "20.00")));

        verify(notifications, never()).save(any());
        verify(userNotifications, never()).save(any());
        verify(users, never()).findActiveSuperAdmins();
    }

    @Test
    void digitalPaymentCreatesNotificationForActiveSuperAdmins() {
        UserEntity superAdmin = UserEntity.builder().id(7L).username("owner").build();
        when(users.findActiveSuperAdmins()).thenReturn(List.of(superAdmin));
        when(users.findAllById(any())).thenReturn(List.of(superAdmin));
        when(notifications.findByEventKey("PAYMENT_RECEIVED:SALE:30:PAYMENT:0"))
                .thenReturn(Optional.empty());
        when(notifications.save(any())).thenAnswer(invocation -> {
            NotificationEntity notification = invocation.getArgument(0);
            notification.setId(90L);
            return notification;
        });
        when(userNotifications.existsByNotificationIdAndUserId(90L, 7L)).thenReturn(false);

        service.createDigitalPaymentNotifications(sale(payment("YAPE", "20.00")));

        ArgumentCaptor<NotificationEntity> notificationCaptor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notifications).save(notificationCaptor.capture());
        NotificationEntity notification = notificationCaptor.getValue();
        assertEquals("PAYMENT_RECEIVED", notification.getType());
        assertEquals("SALE", notification.getReferenceType());
        assertEquals(30L, notification.getReferenceId());
        assertEquals("Pago digital recibido", notification.getTitle());
        assertTrue(notification.getMessage().contains("Yape: S/ 20.00"));

        ArgumentCaptor<UserNotificationEntity> recipientCaptor = ArgumentCaptor.forClass(UserNotificationEntity.class);
        verify(userNotifications).save(recipientCaptor.capture());
        assertEquals(7L, recipientCaptor.getValue().getUser().getId());
    }

    @Test
    void existingPaymentEventDoesNotCreateDuplicateNotificationOrRecipient() {
        UserEntity superAdmin = UserEntity.builder().id(7L).username("owner").build();
        NotificationEntity existing = NotificationEntity.builder()
                .id(90L)
                .eventKey("PAYMENT_RECEIVED:SALE:30:PAYMENT:0")
                .type("PAYMENT_RECEIVED")
                .priority("NORMAL")
                .title("Pago digital recibido")
                .message("Pago existente")
                .build();
        when(users.findActiveSuperAdmins()).thenReturn(List.of(superAdmin));
        when(users.findAllById(any())).thenReturn(List.of(superAdmin));
        when(notifications.findByEventKey(existing.getEventKey())).thenReturn(Optional.of(existing));
        when(userNotifications.existsByNotificationIdAndUserId(90L, 7L)).thenReturn(true);

        service.createDigitalPaymentNotifications(sale(payment("CARD", "15.50")));

        verify(notifications, never()).save(any());
        verify(userNotifications, never()).save(any());
        verify(users).findActiveSuperAdmins();
        verify(users).findAllById(any());
    }

    private SaleDTO sale(SaleDTO.PaymentDTO... payments) {
        return SaleDTO.builder()
                .id(30L)
                .saleNumber("VTA-20260922114205")
                .payments(List.of(payments))
                .build();
    }

    private SaleDTO.PaymentDTO payment(String method, String amount) {
        return SaleDTO.PaymentDTO.builder()
                .paymentMethod(method)
                .amount(new BigDecimal(amount))
                .build();
    }
}
