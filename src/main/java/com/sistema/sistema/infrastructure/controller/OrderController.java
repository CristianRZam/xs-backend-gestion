package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.PageResponseDTO;
import java.time.LocalDate;
import com.sistema.sistema.domain.model.Order;
import com.sistema.sistema.domain.usecase.OrderUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderUseCase orderUseCase;

    public OrderController(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    // ==========================================================
    // CREAR ORDEN
    // ==========================================================

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> create(@RequestBody Order request) {

        Order data = orderUseCase.create(request);
        return ApiResponseFactory.created(data, "Orden creada correctamente");

    }

    // ==========================================================
    // OBTENER ORDEN POR ID
    // ==========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getById(@PathVariable Long id) {

        Order data = orderUseCase.getById(id);
        return ApiResponseFactory.success(data, "Orden obtenida correctamente");

    }

    // ==========================================================
    // OBTENER TODAS LAS ORDENES
    // ==========================================================

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<Order>>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate, @RequestParam(required = false) String status, @RequestParam(required = false) String search) {

        PageResponseDTO<Order> data = orderUseCase.getPage(page, size, fromDate, toDate, status, search);
        return ApiResponseFactory.success(data, "Ordenes obtenidas correctamente");

    }

    // ==========================================================
    // ACTUALIZAR ORDEN
    // ==========================================================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> update(@PathVariable Long id, @RequestBody Order request) {

        Order data = orderUseCase.update(id, request);
        return ApiResponseFactory.success(data, "Orden actualizada correctamente");

    }

    // ==========================================================
    // ACTUALIZAR ESTADO
    // ==========================================================

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {

        String status = request.get("status");
        Order data = orderUseCase.updateStatus(id, status);
        return ApiResponseFactory.success(data, "Estado de la orden actualizado correctamente");

    }

    // ==========================================================
    // ELIMINAR ORDEN
    // ==========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {

        orderUseCase.delete(id);
        return ApiResponseFactory.success(null, "Orden eliminada correctamente");

    }

}
