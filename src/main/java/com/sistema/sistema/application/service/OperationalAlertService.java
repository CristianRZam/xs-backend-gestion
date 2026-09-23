package com.sistema.sistema.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.sistema.application.dto.request.notification.NotificationCreateRequest;
import com.sistema.sistema.domain.usecase.NotificationUseCase;
import com.sistema.sistema.infrastructure.persistence.cash.JpaCashSessionRepository;
import com.sistema.sistema.infrastructure.persistence.inventorycount.JpaInventoryCountSessionRepository;
import com.sistema.sistema.infrastructure.persistence.product.JpaProductRepository;
import com.sistema.sistema.infrastructure.persistence.user.JpaUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class OperationalAlertService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JpaProductRepository products;
    private final JpaCashSessionRepository cashSessions;
    private final JpaInventoryCountSessionRepository inventoryCounts;
    private final JpaUserRepository users;
    private final NotificationUseCase notifications;
    private final ObjectMapper objectMapper;

    @Value("${app.notifications.low-stock-threshold:5}")
    private long lowStockThreshold;
    @Value("${app.notifications.no-movement-days:30}")
    private long noMovementDays;
    @Value("${app.notifications.open-cash-hours:12}")
    private long openCashHours;
    @Value("${app.notifications.pending-count-hours:12}")
    private long pendingCountHours;

    public OperationalAlertService(JpaProductRepository products, JpaCashSessionRepository cashSessions,
                                   JpaInventoryCountSessionRepository inventoryCounts, JpaUserRepository users,
                                   NotificationUseCase notifications, ObjectMapper objectMapper) {
        this.products = products;
        this.cashSessions = cashSessions;
        this.inventoryCounts = inventoryCounts;
        this.users = users;
        this.notifications = notifications;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.notifications.operational-check-interval-ms:900000}",
            initialDelayString = "${app.notifications.operational-check-initial-delay-ms:30000}")
    @Transactional
    public void evaluate() {
        List<Long> superAdminIds = users.findActiveSuperAdmins().stream().map(user -> user.getId()).toList();
        if (superAdminIds.isEmpty()) return;
        LocalDateTime now = LocalDateTime.now();
        String dayKey = LocalDate.now().toString();
        createLowStockAlerts(superAdminIds, dayKey);
        createNoMovementAlerts(superAdminIds, now, dayKey);
        createOpenCashAlerts(superAdminIds, now, dayKey);
        createPendingCountAlerts(superAdminIds, now, dayKey);
    }

    private void createLowStockAlerts(List<Long> recipientIds, String dayKey) {
        products.findByActiveTrueAndDeletedAtIsNullAndTotalStockLessThanEqualOrderByNameAsc(lowStockThreshold)
                .forEach(product -> create(NotificationCreateRequest.builder()
                        .eventKey("LOW_STOCK:PRODUCT:" + product.getId() + ":DATE:" + dayKey)
                        .type("LOW_STOCK").priority(product.getTotalStock() == 0 ? "HIGH" : "NORMAL")
                        .title("Stock bajo")
                        .message(product.getName() + " tiene " + product.getTotalStock() + " unidad(es) disponibles.")
                        .referenceType("PRODUCT").referenceId(product.getId())
                        .metadata(metadata(Map.of("productName", product.getName(), "productCode", product.getCode(),
                                "currentStock", product.getTotalStock(), "threshold", lowStockThreshold)))
                        .build(), recipientIds));
    }

    private void createNoMovementAlerts(List<Long> recipientIds, LocalDateTime now, String dayKey) {
        LocalDateTime since = now.minusDays(noMovementDays);
        products.findActiveProductsWithoutMovementSince(since, since).forEach(product -> create(
                NotificationCreateRequest.builder()
                        .eventKey("NO_MOVEMENT:PRODUCT:" + product.getId() + ":DATE:" + dayKey)
                        .type("NO_MOVEMENT").priority("LOW").title("Producto sin movimiento")
                        .message(product.getName() + " no registra movimientos desde hace " + noMovementDays + " días.")
                        .referenceType("PRODUCT").referenceId(product.getId())
                        .metadata(metadata(Map.of("productName", product.getName(), "productCode", product.getCode(),
                                "daysWithoutMovement", noMovementDays)))
                        .build(), recipientIds));
    }

    private void createOpenCashAlerts(List<Long> recipientIds, LocalDateTime now, String dayKey) {
        cashSessions.findByStatusAndDeletedAtIsNullAndOpenedAtBefore("OPEN", now.minusHours(openCashHours))
                .forEach(session -> create(NotificationCreateRequest.builder()
                        .eventKey("CASH_SESSION_OPEN:SESSION:" + session.getId() + ":DATE:" + dayKey)
                        .type("CASH_SESSION_OPEN").priority("HIGH").title("Caja abierta pendiente de cierre")
                        .message("La caja #" + session.getCashRegisterId() + " continúa abierta desde "
                                + TIME_FORMAT.format(session.getOpenedAt()) + ".")
                        .referenceType("CASH_SESSION").referenceId(session.getId())
                        .metadata(metadata(Map.of("cashRegisterId", session.getCashRegisterId(),
                                "openedAt", session.getOpenedAt().toString(), "openHours", openCashHours)))
                        .build(), recipientIds));
    }

    private void createPendingCountAlerts(List<Long> recipientIds, LocalDateTime now, String dayKey) {
        inventoryCounts.findByStatusInAndOpenedAtBefore(List.of("OPEN", "REVIEW"), now.minusHours(pendingCountHours))
                .forEach(count -> create(NotificationCreateRequest.builder()
                        .eventKey("INVENTORY_COUNT_PENDING:SESSION:" + count.getId() + ":DATE:" + dayKey)
                        .type("INVENTORY_COUNT_PENDING").priority("HIGH").title("Conteo de inventario pendiente")
                        .message("El conteo " + count.getCountNumber() + " sigue en estado " + count.getStatus()
                                + " desde " + TIME_FORMAT.format(count.getOpenedAt()) + ".")
                        .referenceType("INVENTORY_COUNT").referenceId(count.getId())
                        .metadata(metadata(Map.of("countNumber", count.getCountNumber() == null ? "" : count.getCountNumber(),
                                "status", count.getStatus(), "openedAt", count.getOpenedAt().toString(),
                                "pendingHours", pendingCountHours)))
                        .build(), recipientIds));
    }

    private void create(NotificationCreateRequest request, List<Long> recipientIds) {
        notifications.create(request, recipientIds);
    }

    private String metadata(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No fue posible preparar los datos de la alerta operativa.", exception);
        }
    }
}
