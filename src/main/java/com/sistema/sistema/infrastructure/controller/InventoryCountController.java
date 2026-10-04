package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.request.inventorycount.InventoryCountCloseRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.domain.usecase.InventoryCountUseCase;
import com.sistema.sistema.infrastructure.persistence.inventorycount.InventoryCountSessionEntity;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory-counts")
@Slf4j
public class InventoryCountController {
    private final InventoryCountUseCase service;
    public InventoryCountController(InventoryCountUseCase service) { 
        this.service = service; 
    }
    @PostMapping 
    public ResponseEntity<ApiResponse<InventoryCountSessionEntity>> open(@RequestBody(required = false) Map<String,String> body) { 
        
        InventoryCountSessionEntity data = service.open(body == null ? null : body.get("comment"));
        log.info("Conteo de inventario iniciado: countId={}", data.getId());
        return ApiResponseFactory.created(data, "Conteo iniciado correctamente"); 
    }
    
    @GetMapping("/current") 
    public ResponseEntity<ApiResponse<InventoryCountSessionEntity>> current() { 
       
        return ApiResponseFactory.success(service.current(), "Conteo activo obtenido correctamente"); 
    
    }
    
    @GetMapping 
    public ResponseEntity<ApiResponse<List<InventoryCountSessionEntity>>> history() { 
        
        return ApiResponseFactory.success(service.history(), "Historial obtenido correctamente"); 
    
    }
    
    @GetMapping("/{id}") 
    public ResponseEntity<ApiResponse<Map<String,Object>>> detail(@PathVariable Long id) { 

        
        return ApiResponseFactory.success(service.detail(id), "Conteo obtenido correctamente"); 
        
    }
    
    @PutMapping("/{id}/review") 
    public ResponseEntity<ApiResponse<Map<String,Object>>> review(@PathVariable Long id, @RequestBody InventoryCountCloseRequest request) { 
        
        log.info("Conteo de inventario enviado a revisión: countId={}", id);
        return ApiResponseFactory.success(service.review(id, request), "Diferencias calculadas correctamente"); 
        
    }
    
    @PutMapping("/{id}/close") 
    public ResponseEntity<ApiResponse<InventoryCountSessionEntity>> close(@PathVariable Long id, @RequestBody InventoryCountCloseRequest request) { 
        
        InventoryCountSessionEntity data = service.close(id, request);
        log.info("Conteo de inventario finalizado: countId={}", id);
        return ApiResponseFactory.success(data, "Conteo procesado correctamente"); 
        
    }

}
