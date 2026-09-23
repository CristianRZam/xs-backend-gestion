package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.notification.NotificationCreateRequest;
import com.sistema.sistema.application.dto.response.notification.NotificationDTO;
import com.sistema.sistema.application.dto.response.notification.NotificationUnreadCountDTO;
import com.sistema.sistema.application.dto.response.notification.NotificationConfigurationDTO;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;

import java.util.Collection;
import java.time.LocalDate;
import java.util.List;

public interface NotificationUseCase {

    NotificationDTO create(NotificationCreateRequest request, Collection<Long> recipientUserIds);

    void createDigitalPaymentNotifications(SaleDTO sale);

    List<NotificationDTO> findForUser(Long userId);

    List<NotificationDTO> findForUser(
            Long userId,
            String type,
            String priority,
            Boolean read,
            LocalDate fromDate,
            LocalDate toDate,
            String search
    );

    List<NotificationDTO> findForCurrentSuperAdmin();

    List<NotificationDTO> findForCurrentSuperAdmin(
            String type,
            String priority,
            Boolean read,
            LocalDate fromDate,
            LocalDate toDate,
            String search
    );

    NotificationUnreadCountDTO countUnreadForUser(Long userId);

    NotificationUnreadCountDTO countUnreadForCurrentSuperAdmin();

    NotificationConfigurationDTO getConfigurationForCurrentSuperAdmin();

    void markAsRead(Long notificationId, Long userId);

    void markAsReadForCurrentSuperAdmin(Long notificationId);

    void markAllAsRead(Long userId);

    void markAllAsReadForCurrentSuperAdmin();
}
