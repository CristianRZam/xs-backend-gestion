package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.request.Role.RoleCreateRequest;
import com.sistema.sistema.application.dto.request.Role.RoleUpdateRequest;
import com.sistema.sistema.application.dto.request.Role.RoleViewRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.RoleViewResponse;
import com.sistema.sistema.application.report.RoleReportService;
import com.sistema.sistema.domain.model.Role;
import com.sistema.sistema.domain.usecase.RoleUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;


@RestController
@RequestMapping("/api/role")
public class RoleController {
    private final RoleUseCase roleUseCase;
    private final RoleReportService reportService;

    public RoleController(RoleUseCase roleUseCase, RoleReportService reportService) {
        this.roleUseCase = roleUseCase;
        this.reportService = reportService;
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
        return ApiResponseFactory.success(createdRole, "Rol actualizado correctamente");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        boolean response = roleUseCase.delete(id);
        return ApiResponseFactory.success(response, "Rol eliminado correctamente");
    }

    @PutMapping("/update-status")
    public ResponseEntity<ApiResponse<Boolean>> updateStatus(@RequestBody Long id) {
        Boolean response = roleUseCase.updateStatus(id);
        return ApiResponseFactory.success(response, "Estado de rol actualizado correctamente");
    }

    @PostMapping("/export-pdf")
    public ResponseEntity<InputStreamResource> generateRolesReport(@RequestBody RoleViewRequest request) {
        byte[] pdf = reportService.generatePdfReport(request, "usuario_demo");

        ByteArrayInputStream bis = new ByteArrayInputStream(pdf);
        InputStreamResource resource = new InputStreamResource(bis);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=roles_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(resource);
    }

    @PostMapping("/export-excel")
    public ResponseEntity<InputStreamResource> generateRolesExcel(@RequestBody RoleViewRequest request) {
        byte[] excel = reportService.generateExcelReport(request, "usuario_demo");

        ByteArrayInputStream bis = new ByteArrayInputStream(excel);
        InputStreamResource resource = new InputStreamResource(bis);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=roles_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excel.length)
                .body(resource);
    }


}
