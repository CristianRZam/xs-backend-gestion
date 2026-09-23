package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.notification.NotificationCreateRequest;
import com.sistema.sistema.application.dto.response.notification.NotificationDTO;
import com.sistema.sistema.application.dto.response.notification.NotificationUnreadCountDTO;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;

import java.util.Collection;
import java.util.List;

public interface NotificationUseCase {

    NotificationDTO create(NotificationCreateRequest request, Collection<Long> recipientUserIds);

    void createDigitalPaymentNotifications(SaleDTO sale);

    List<NotificationDTO> findForUser(Long userId);

    List<NotificationDTO> findForCurrentSuperAdmin();

    NotificationUnreadCountDTO countUnreadForUser(Long userId);

    NotificationUnreadCountDTO countUnreadForCurrentSuperAdmin();

    void markAsRead(Long notificationId, Long userId);

    void markAsReadForCurrentSuperAdmin(Long notificationId);

    void markAllAsRead(Long userId);

    void markAllAsReadForCurrentSuperAdmin();
}
