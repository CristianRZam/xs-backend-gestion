package com.sistema.sistema.infrastructure.controller;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.dashboard.DashboardDTO;
import com.sistema.sistema.domain.usecase.DashboardUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardUseCase useCase;

    public DashboardController(DashboardUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<DashboardDTO>> get() {
        return ApiResponseFactory.success(
                useCase.getSummary(),
                "Dashboard obtenido correctamente"
        );
    }
}
