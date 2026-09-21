package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.request.sale.SaleCreateRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;
import com.sistema.sistema.domain.usecase.SaleUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.sistema.sistema.application.dto.response.sale.CashSessionSalesSummaryDTO;

@RestController
@RequestMapping("/api/sales")
public class SaleController {
    private final SaleUseCase saleUseCase;
    public SaleController(SaleUseCase saleUseCase) { this.saleUseCase = saleUseCase; }
    @PostMapping public ResponseEntity<ApiResponse<SaleDTO>> create(@Valid @RequestBody SaleCreateRequest request) {
        return ApiResponseFactory.created(saleUseCase.create(request), "Venta registrada correctamente");
    }
    @GetMapping public ResponseEntity<ApiResponse<List<SaleDTO>>> getAll() {
        return ApiResponseFactory.success(saleUseCase.getAll(), "Ventas obtenidas correctamente");
    }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<SaleDTO>> getById(@PathVariable Long id) {
        return ApiResponseFactory.success(saleUseCase.getById(id), "Venta obtenida correctamente");
    }

    @GetMapping("/cash-session/{cashSessionId}")
    public ResponseEntity<ApiResponse<CashSessionSalesSummaryDTO>> getCashSessionSales(@PathVariable Long cashSessionId) {
        return ApiResponseFactory.success(saleUseCase.getCashSessionSummary(cashSessionId), "Resumen de ventas de caja obtenido correctamente");
    }
}
