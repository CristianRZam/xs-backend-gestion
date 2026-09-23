package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.domain.model.CashSession;
import com.sistema.sistema.application.dto.response.PageResponseDTO;
import com.sistema.sistema.domain.usecase.CashSessionUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cash-session")
public class CashSessionController {

    private final CashSessionUseCase cashSessionUseCase;

    public CashSessionController(CashSessionUseCase cashSessionUseCase) {
        this.cashSessionUseCase = cashSessionUseCase;
    }

    // ABRIR SESIÓN
    @PostMapping("/open")
    public ResponseEntity<ApiResponse<CashSession>> openSession(@RequestBody CashSession request) {

        CashSession data = cashSessionUseCase.openSession(request);
        return ApiResponseFactory.created(data, "Caja abierta correctamente");

    }

    // OBTENER SESIÓN ACTUAL
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<CashSession>> getCurrentSession() {

        CashSession data = cashSessionUseCase.getCurrentSession();
        return ApiResponseFactory.success(data, "Sesión de caja obtenida correctamente");

    }

    // VERIFICAR SI EXISTE SESIÓN ABIERTA
    @GetMapping("/exists-open")
    public ResponseEntity<ApiResponse<Boolean>> existsOpenSession() {

        boolean exists = cashSessionUseCase.existsOpenSession();
        return ApiResponseFactory.success(exists, "Estado de la sesión de caja obtenido correctamente");

    }

    // CERRAR SESIÓN
    @PutMapping("/close/{id}")
    public ResponseEntity<ApiResponse<CashSession>> closeSession(@PathVariable Long id, @RequestParam BigDecimal closingAmount, @RequestParam(required = false) BigDecimal expectedAmount,
            @RequestParam(required = false) BigDecimal difference, @RequestParam(required = false) String closingComment ) {

        CashSession data = cashSessionUseCase.closeSession(id, closingAmount, expectedAmount, difference, closingComment);
        return ApiResponseFactory.success(data, "Caja cerrada correctamente");

    }

    // HISTORIAL DE SESIONES
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponseDTO<CashSession>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponseDTO<CashSession> data = cashSessionUseCase.getHistory(page, size);
        return ApiResponseFactory.success(data, "Historial de sesiones obtenido correctamente");

    }

}
