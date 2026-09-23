package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.notification.NotificationDTO;
import com.sistema.sistema.application.dto.response.notification.NotificationUnreadCountDTO;
import com.sistema.sistema.application.dto.response.notification.NotificationConfigurationDTO;
import com.sistema.sistema.domain.usecase.NotificationUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationUseCase notificationUseCase;

    public NotificationController(NotificationUseCase notificationUseCase) {
        this.notificationUseCase = notificationUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String search
    ) {
        return ApiResponseFactory.success(
                notificationUseCase.findForCurrentSuperAdmin(type, priority, read, fromDate, toDate, search),
                "Notificaciones obtenidas correctamente."
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<NotificationUnreadCountDTO>> getUnreadCount() {
        return ApiResponseFactory.success(
                notificationUseCase.countUnreadForCurrentSuperAdmin(),
                "Cantidad de notificaciones no leídas obtenida correctamente."
        );
    }

    @GetMapping("/configuration")
    public ResponseEntity<ApiResponse<NotificationConfigurationDTO>> getConfiguration() {
        return ApiResponseFactory.success(
                notificationUseCase.getConfigurationForCurrentSuperAdmin(),
                "Configuración de notificaciones obtenida correctamente."
        );
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationUseCase.markAsReadForCurrentSuperAdmin(id);
        return ApiResponseFactory.success(null, "Notificación marcada como leída.");
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationUseCase.markAllAsReadForCurrentSuperAdmin();
        return ApiResponseFactory.success(null, "Todas las notificaciones fueron marcadas como leídas.");
    }
}
