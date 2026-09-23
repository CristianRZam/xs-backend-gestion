package com.sistema.sistema.infrastructure.controller;


import com.sistema.sistema.application.dto.request.inventorymovement.InventoryMovementCreateRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementDTO;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementPageDTO;
import com.sistema.sistema.domain.usecase.InventoryMovementUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/inventory-movement")
public class InventoryMovementController {


    private final InventoryMovementUseCase inventoryMovementUseCase;


    public InventoryMovementController(
            InventoryMovementUseCase inventoryMovementUseCase
    ) {
        this.inventoryMovementUseCase = inventoryMovementUseCase;
    }


    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<InventoryMovementPageDTO>> findAll(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        InventoryMovementPageDTO movements = inventoryMovementUseCase.findPage(productId, page, size);

        return ApiResponseFactory.success(movements, "Movimientos de inventario cargados correctamente.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryMovementDTO>> findById(@PathVariable Long id) {
        InventoryMovementDTO movement = inventoryMovementUseCase.findById(id);


        return ApiResponseFactory.success(movement, "Movimiento de inventario encontrado correctamente.");
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<InventoryMovementDTO>> create(@Valid @RequestBody InventoryMovementCreateRequest request) {
        InventoryMovementDTO movement = inventoryMovementUseCase.create(request);

        return ApiResponseFactory.created(movement, "Movimiento de inventario registrado correctamente.");
    }

}
