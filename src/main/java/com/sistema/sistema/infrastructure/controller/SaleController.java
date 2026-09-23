package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.request.sale.SaleCreateRequest;
import com.sistema.sistema.application.dto.request.sale.SaleCancellationRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;
import com.sistema.sistema.application.dto.response.PageResponseDTO;
import java.time.LocalDate;
import com.sistema.sistema.domain.usecase.SaleUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.sistema.sistema.application.dto.response.sale.CashSessionSalesSummaryDTO;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/sales")
public class SaleController {
    private final SaleUseCase saleUseCase;
    public SaleController(SaleUseCase saleUseCase) { this.saleUseCase = saleUseCase; }
    @PostMapping public ResponseEntity<ApiResponse<SaleDTO>> create(@Valid @RequestBody SaleCreateRequest request) {
        return ApiResponseFactory.created(saleUseCase.create(request), "Venta registrada correctamente");
    }
    @GetMapping public ResponseEntity<ApiResponse<PageResponseDTO<SaleDTO>>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate) {
        return ApiResponseFactory.success(saleUseCase.getPage(page, size, fromDate, toDate), "Ventas obtenidas correctamente");
    }
    @GetMapping("/{id}") public ResponseEntity<ApiResponse<SaleDTO>> getById(@PathVariable Long id) {
        return ApiResponseFactory.success(saleUseCase.getById(id), "Venta obtenida correctamente");
    }
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SaleDTO>> cancel(@PathVariable Long id,
                                                        @Valid @RequestBody SaleCancellationRequest request) {
        return ApiResponseFactory.success(saleUseCase.cancel(id, request), "Venta anulada correctamente");
    }

    @GetMapping("/cash-session/{cashSessionId}")
    public ResponseEntity<ApiResponse<CashSessionSalesSummaryDTO>> getCashSessionSales(@PathVariable Long cashSessionId) {
        return ApiResponseFactory.success(saleUseCase.getCashSessionSummary(cashSessionId), "Resumen de ventas de caja obtenido correctamente");
    }
}
