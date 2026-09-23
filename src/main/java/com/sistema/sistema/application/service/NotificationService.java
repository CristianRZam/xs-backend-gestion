package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.notification.NotificationCreateRequest;
import com.sistema.sistema.application.dto.response.notification.NotificationDTO;
import com.sistema.sistema.application.dto.response.notification.NotificationUnreadCountDTO;
import com.sistema.sistema.application.dto.response.notification.NotificationConfigurationDTO;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;
import com.sistema.sistema.domain.usecase.NotificationUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import com.sistema.sistema.infrastructure.persistence.notification.JpaNotificationRepository;
import com.sistema.sistema.infrastructure.persistence.notification.JpaUserNotificationRepository;
import com.sistema.sistema.infrastructure.persistence.notification.NotificationEntity;
import com.sistema.sistema.infrastructure.persistence.notification.UserNotificationEntity;
import com.sistema.sistema.infrastructure.persistence.user.JpaUserRepository;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NotificationService implements NotificationUseCase {

    private final JpaNotificationRepository notifications;
    private final JpaUserNotificationRepository userNotifications;
    private final JpaUserRepository users;
    private final ObjectMapper objectMapper;

    @Value("${app.notifications.visible-days:7}")
    private long visibleDays;

    public NotificationService(
            JpaNotificationRepository notifications,
            JpaUserNotificationRepository userNotifications,
            JpaUserRepository users,
            ObjectMapper objectMapper
    ) {
        this.notifications = notifications;
        this.userNotifications = userNotifications;
        this.users = users;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void createDigitalPaymentNotifications(SaleDTO sale) {
        boolean hasDigitalPayment = sale.getPayments().stream()
                .map(SaleDTO.PaymentDTO::getPaymentMethod)
                .map(this::normalizePaymentMethod)
                .anyMatch(this::isDigitalPayment);
        if (!hasDigitalPayment) {
            return;
        }

        List<Long> superAdminIds = users.findActiveSuperAdmins().stream()
                .map(user -> user.getId())
                .toList();

        if (superAdminIds.isEmpty()) {
            return;
        }

        for (int paymentIndex = 0; paymentIndex < sale.getPayments().size(); paymentIndex++) {
            SaleDTO.PaymentDTO payment = sale.getPayments().get(paymentIndex);
            String paymentMethod = normalizePaymentMethod(payment.getPaymentMethod());

            if (!isDigitalPayment(paymentMethod)) {
                continue;
            }

            create(NotificationCreateRequest.builder()
                    .eventKey("PAYMENT_RECEIVED:SALE:" + sale.getId() + ":PAYMENT:" + paymentIndex)
                    .type("PAYMENT_RECEIVED")
                    .priority("NORMAL")
                    .title("Pago digital recibido")
                    .message(paymentLabel(paymentMethod) + ": S/ " + formatAmount(payment.getAmount())
                            + " · Venta " + sale.getSaleNumber())
                    .referenceType("SALE")
                    .referenceId(sale.getId())
                    .metadata(paymentMetadata(sale, payment, paymentMethod))
                    .build(), superAdminIds);
        }
    }

    @Override
    @Transactional
    public NotificationDTO create(NotificationCreateRequest request, Collection<Long> recipientUserIds) {
        NotificationEntity notification = notifications.findByEventKey(request.getEventKey())
                .orElseGet(() -> notifications.save(NotificationEntity.builder()
                        .eventKey(request.getEventKey())
                        .type(request.getType())
                        .priority(request.getPriority())
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .referenceType(request.getReferenceType())
                        .referenceId(request.getReferenceId())
                        .metadata(request.getMetadata())
                        .build()));

        Set<Long> uniqueRecipientIds = recipientUserIds == null
                ? Set.of()
                : Set.copyOf(recipientUserIds);

        if (!uniqueRecipientIds.isEmpty()) {
            users.findAllById(uniqueRecipientIds).forEach(user -> {
                if (!userNotifications.existsByNotificationIdAndUserId(notification.getId(), user.getId())) {
                    userNotifications.save(UserNotificationEntity.builder()
                            .notification(notification)
                            .user(user)
                            .build());
                }
            });
        }

        return toDto(notification, false, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> findForUser(Long userId) {
        return findForUser(userId, null, null, null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> findForUser(
            Long userId,
            String type,
            String priority,
            Boolean read,
            LocalDate fromDate,
            LocalDate toDate,
            String search
    ) {
        LocalDateTime createdSince = fromDate == null
                ? visibleSince()
                : max(visibleSince(), fromDate.atStartOfDay());
        LocalDateTime createdBefore = toDate == null ? null : toDate.plusDays(1).atStartOfDay();

        Specification<UserNotificationEntity> specification = (root, query, builder) -> {
            Join<UserNotificationEntity, NotificationEntity> notification = root.join("notification");
            var predicate = builder.and(
                    builder.equal(root.get("user").get("id"), userId),
                    builder.isNull(root.get("dismissedAt")),
                    builder.greaterThanOrEqualTo(notification.get("createdAt"), createdSince)
            );
            if (hasText(type)) {
                predicate = builder.and(predicate, builder.equal(notification.get("type"), normalize(type)));
            }
            if (hasText(priority)) {
                predicate = builder.and(predicate, builder.equal(notification.get("priority"), normalize(priority)));
            }
            if (read != null) {
                predicate = read
                        ? builder.and(predicate, builder.isNotNull(root.get("readAt")))
                        : builder.and(predicate, builder.isNull(root.get("readAt")));
            }
            if (createdBefore != null) {
                predicate = builder.and(predicate, builder.lessThan(notification.get("createdAt"), createdBefore));
            }
            if (hasText(search)) {
                String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicate = builder.and(predicate, builder.or(
                        builder.like(builder.lower(notification.get("title")), term),
                        builder.like(builder.lower(notification.get("message")), term),
                        builder.like(builder.lower(builder.coalesce(notification.get("referenceType"), "")), term)
                ));
            }
            return predicate;
        };

        return userNotifications.findAll(
                        specification,
                        Sort.by(Sort.Order.desc("notification.createdAt"), Sort.Order.desc("notification.id"))
                )
                .stream()
                .map(userNotification -> toDto(
                        userNotification.getNotification(),
                        userNotification.getReadAt() != null,
                        userNotification.getReadAt()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> findForCurrentSuperAdmin() {
        return findForUser(requireCurrentSuperAdminId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> findForCurrentSuperAdmin(
            String type,
            String priority,
            Boolean read,
            LocalDate fromDate,
            LocalDate toDate,
            String search
    ) {
        return findForUser(requireCurrentSuperAdminId(), type, priority, read, fromDate, toDate, search);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationUnreadCountDTO countUnreadForUser(Long userId) {
        return new NotificationUnreadCountDTO(
                userNotifications.countByUserIdAndReadAtIsNullAndDismissedAtIsNullAndNotificationCreatedAtGreaterThanEqual(
                        userId,
                        visibleSince()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationUnreadCountDTO countUnreadForCurrentSuperAdmin() {
        return countUnreadForUser(requireCurrentSuperAdminId());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationConfigurationDTO getConfigurationForCurrentSuperAdmin() {
        requireCurrentSuperAdminId();
        return new NotificationConfigurationDTO(Math.max(visibleDays, 1));
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        UserNotificationEntity userNotification = userNotifications
                .findByNotificationIdAndUserIdAndDismissedAtIsNull(notificationId, userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Notificación no encontrada."));

        if (userNotification.getReadAt() == null) {
            userNotification.setReadAt(LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public void markAsReadForCurrentSuperAdmin(Long notificationId) {
        markAsRead(notificationId, requireCurrentSuperAdminId());
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        userNotifications.markAllAsRead(userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void markAllAsReadForCurrentSuperAdmin() {
        markAllAsRead(requireCurrentSuperAdminId());
    }

    private NotificationDTO toDto(NotificationEntity notification, boolean read, LocalDateTime readAt) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .priority(notification.getPriority())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .metadata(notification.getMetadata())
                .createdAt(notification.getCreatedAt())
                .read(read)
                .readAt(readAt)
                .build();
    }

    private boolean isDigitalPayment(String paymentMethod) {
        return Set.of("YAPE", "CARD", "TRANSFER").contains(paymentMethod);
    }

    private String normalizePaymentMethod(String paymentMethod) {
        return paymentMethod == null ? "" : paymentMethod.trim().toUpperCase(Locale.ROOT);
    }

    private String paymentLabel(String paymentMethod) {
        return switch (paymentMethod) {
            case "YAPE" -> "Yape";
            case "CARD" -> "Tarjeta";
            case "TRANSFER" -> "Transferencia";
            default -> paymentMethod;
        };
    }

    private String formatAmount(java.math.BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String paymentMetadata(SaleDTO sale, SaleDTO.PaymentDTO payment, String paymentMethod) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "saleNumber", sale.getSaleNumber(),
                    "paymentMethod", paymentMethod,
                    "amount", payment.getAmount().toPlainString(),
                    "reference", payment.getReference() == null ? "" : payment.getReference()
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No fue posible preparar los datos de la notificación.", exception);
        }
    }

    private Long requireCurrentSuperAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = SecurityUtil.getCurrentUserId();

        if (authentication == null || !authentication.isAuthenticated() || userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión.");
        }

        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SUPER_ADMIN".equals(authority.getAuthority()));
        if (!isSuperAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Solo un superadministrador puede consultar notificaciones.");
        }

        return userId;
    }

    private LocalDateTime visibleSince() {
        return LocalDateTime.now().minusDays(Math.max(visibleDays, 1));
    }

    private LocalDateTime max(LocalDateTime first, LocalDateTime second) {
        return first.isAfter(second) ? first : second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
