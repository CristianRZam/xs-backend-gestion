package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.request.catalogconfig.CatalogConfigCreateRequest;
import com.sistema.sistema.application.dto.request.catalogconfig.CatalogConfigUpdateRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.catalogconfig.CatalogConfigDTO;
import com.sistema.sistema.domain.usecase.CatalogConfigUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog-config")
public class CatalogConfigController {

    private final CatalogConfigUseCase catalogConfigUseCase;

    public CatalogConfigController(CatalogConfigUseCase catalogConfigUseCase) {
        this.catalogConfigUseCase = catalogConfigUseCase;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CatalogConfigDTO>>> findAll() {
        List<CatalogConfigDTO> result = catalogConfigUseCase.findAll();
        return ApiResponseFactory.success(result, "Configuraciones obtenidas correctamente");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CatalogConfigDTO>> findById(@PathVariable Long id) {
        CatalogConfigDTO result = catalogConfigUseCase.findById(id);
        return ApiResponseFactory.success(result, "Configuración obtenida correctamente");
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CatalogConfigDTO>> create(
            @Valid @RequestBody CatalogConfigCreateRequest request
    ) {
        CatalogConfigDTO result = catalogConfigUseCase.create(request);
        return ApiResponseFactory.created(result, "Configuración creada correctamente");
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<CatalogConfigDTO>> update(
            @Valid @RequestBody CatalogConfigUpdateRequest request
    ) {
        CatalogConfigDTO result = catalogConfigUseCase.update(request);
        return ApiResponseFactory.success(result, "Configuración actualizada correctamente");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        boolean result = catalogConfigUseCase.delete(id);
        return ApiResponseFactory.success(result, "Configuración eliminada correctamente");
    }
}