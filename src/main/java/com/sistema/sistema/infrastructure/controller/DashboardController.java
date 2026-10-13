package com.sistema.sistema.infrastructure.controller;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.dashboard.DashboardDTO;
import com.sistema.sistema.application.dto.response.dashboard.DashboardSellerDTO;
import com.sistema.sistema.domain.usecase.DashboardUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/dashboard")
@Slf4j
public class DashboardController {
    private final DashboardUseCase useCase;

    public DashboardController(DashboardUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<DashboardDTO>> get(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "GLOBAL") String scope,
            @RequestParam(required = false) Long sellerId) {
        log.debug("Solicitud autorizada para obtener el dashboard");
        return ApiResponseFactory.success(
                useCase.getSummary(fromDate, toDate, scope, sellerId),
                "Dashboard obtenido correctamente"
        );
    }

    @GetMapping(value = "/sellers", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<DashboardSellerDTO>>> sellers() {
        return ApiResponseFactory.success(useCase.getSellers(), "Vendedores obtenidos correctamente");
    }
}
