package com.sistema.sistema.infrastructure.controller;


import com.sistema.sistema.application.dto.request.user.UserCreateRequest;
import com.sistema.sistema.application.dto.request.user.UserFormRequest;
import com.sistema.sistema.application.dto.request.user.UserUpdateRequest;
import com.sistema.sistema.application.dto.request.user.UserViewRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.user.UserFormResponse;
import com.sistema.sistema.application.dto.response.user.UserViewResponse;
import com.sistema.sistema.application.report.UserReportService;
import com.sistema.sistema.domain.model.User;
import com.sistema.sistema.domain.usecase.UserUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserUseCase userUseCase;
    private final UserReportService reportService;
    public UserController(UserUseCase userUseCase, UserReportService reportService) {
        this.userUseCase = userUseCase;
        this.reportService = reportService;
    }

    @PreAuthorize("hasAuthority('VIEW_USER')")
    @PostMapping("/init")
    public ResponseEntity<ApiResponse<UserViewResponse>> initRolesView(@RequestBody UserViewRequest request) {
        UserViewResponse data = userUseCase.init(request);
        return ApiResponseFactory.success(data, "Lista de usuarios y resumen cargados correctamente");
    }

    @PreAuthorize("hasAuthority('CREATE_USER') or hasAuthority('EDIT_USER')")
    @PostMapping("/init-form")
    public ResponseEntity<ApiResponse<UserFormResponse>> initFormData(@RequestBody UserFormRequest request) {
        UserFormResponse data = userUseCase.initFormData(request);
        return ApiResponseFactory.success(data, "Datos de inicialización del formulario cargados correctamente");
    }


    @GetMapping("/get/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user =userUseCase.getUserById(id);
        return ApiResponseFactory.success(user, "Usuario obtenido correctamente");
    }

    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<User>> create(@Valid @RequestBody UserCreateRequest request) {
        User data = userUseCase.create(request);
        return ApiResponseFactory.created(data, "Usuario creado correctamente");
    }

    @PreAuthorize("hasAuthority('EDIT_USER')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<User>> update(@Valid @RequestBody UserUpdateRequest request) {
        User data = userUseCase.update(request);
        return ApiResponseFactory.success(data, "Usuario actualizado correctamente");
    }

    @PreAuthorize("hasAuthority('DELETE_USER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        Boolean response = userUseCase.delete(id);
        return ApiResponseFactory.success(response, "Usuario eliminado correctamente");
    }

    @PreAuthorize("hasAuthority('EDIT_USER')")
    @PutMapping("/update-status")
    public ResponseEntity<ApiResponse<Boolean>> updateStatus(@RequestBody Long id) {
        Boolean response = userUseCase.updateStatus(id);
        return ApiResponseFactory.success(response, "Estado de usuario actualizado correctamente");
    }

    @PreAuthorize("hasAuthority('EXPORT_USER')")
    @PostMapping("/export-pdf")
    public ResponseEntity<InputStreamResource> generateRolesReport(@RequestBody UserViewRequest request) {
        byte[] pdf = reportService.generatePdfReport(request);

        ByteArrayInputStream bis = new ByteArrayInputStream(pdf);
        InputStreamResource resource = new InputStreamResource(bis);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=roles_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(resource);
    }

    @PreAuthorize("hasAuthority('EXPORT_USER')")
    @PostMapping("/export-excel")
    public ResponseEntity<InputStreamResource> generateRolesExcel(@RequestBody UserViewRequest request) {
        byte[] excel = reportService.generateExcelReport(request);

        ByteArrayInputStream bis = new ByteArrayInputStream(excel);
        InputStreamResource resource = new InputStreamResource(bis);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=roles_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excel.length)
                .body(resource);
    }
}
