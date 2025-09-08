package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.request.Role.RoleCreateRequest;
import com.sistema.sistema.application.dto.request.Role.RoleUpdateRequest;
import com.sistema.sistema.application.dto.request.Role.RoleViewRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.RoleViewResponse;
import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.service.RoleUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/role")
public class RoleController {
    private final RoleUseCase roleUseCase;

    public RoleController(RoleUseCase roleUseCase) {
        this.roleUseCase = roleUseCase;
    }

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<RoleViewResponse>> initRolesView(@RequestBody RoleViewRequest request) {
        RoleViewResponse data = roleUseCase.init(request);
        return ApiResponseFactory.success(data, "Lista de roles y resumen de estado cargados correctamente");
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ApiResponse<Role>> getRoleById(@PathVariable Long id) {
        Role role = roleUseCase.getRoleById(id);
        return ApiResponseFactory.success(role, "Rol obtenido correctamente");
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Role>> create(@Valid @RequestBody RoleCreateRequest request) {
        Role createdRole = roleUseCase.create(request);
        return ApiResponseFactory.created(createdRole, "Rol creado correctamente");
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Role>> update(@Valid @RequestBody RoleUpdateRequest request) {
        Role createdRole = roleUseCase.update(request);
        return ApiResponseFactory.success(createdRole, "Rol creado correctamente");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        boolean response = roleUseCase.delete(id);
        return ApiResponseFactory.success(response, "Rol eliminado correctamente");
    }


}
