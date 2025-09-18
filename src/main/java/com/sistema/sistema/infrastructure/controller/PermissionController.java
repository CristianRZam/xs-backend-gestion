package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.request.Permission.PermissionUpdateRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.permission.PermissionViewResponse;
import com.sistema.sistema.domain.usecase.PermissionUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {
    private final PermissionUseCase permissionUseCase;
    public PermissionController(PermissionUseCase permissionUseCase){
        this.permissionUseCase = permissionUseCase;
    }

    @GetMapping("/get-by-role/{id}")
    public ResponseEntity<ApiResponse<PermissionViewResponse>> getByRoleId(@PathVariable Long id) {
        PermissionViewResponse response = permissionUseCase.getByRoleId(id);
        return ApiResponseFactory.success(response, "Rol obtenido correctamente");
    }

    @PutMapping("/update-permission-by-role")
    public ResponseEntity<ApiResponse<Boolean>> updatePermissionByRole(@Valid @RequestBody PermissionUpdateRequest request) {
        Boolean response = permissionUseCase.updatePermissionByRole(request);
        return ApiResponseFactory.success(response, "Permisos actualizados.");
    }


}
