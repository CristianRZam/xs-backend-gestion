package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.request.parameter.ParameterCreateRequest;
import com.sistema.sistema.application.dto.request.parameter.ParameterFormRequest;
import com.sistema.sistema.application.dto.request.parameter.ParameterUpdateRequest;
import com.sistema.sistema.application.dto.request.parameter.ParameterViewRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.parameter.ParameterFormResponse;
import com.sistema.sistema.application.dto.response.parameter.ParameterViewResponse;
import com.sistema.sistema.application.report.ParameterReportService;
import com.sistema.sistema.domain.model.Parameter;
import com.sistema.sistema.domain.usecase.ParameterUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;


@RestController
@RequestMapping("/api/parameter")
public class ParameterController {
    private final ParameterUseCase parameterUseCase;
    private final ParameterReportService reportService;

    public ParameterController(ParameterUseCase parameterUseCase, ParameterReportService reportService){
        this.parameterUseCase = parameterUseCase;
        this.reportService = reportService;
    }

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<ParameterViewResponse>> initParametersView(@RequestBody ParameterViewRequest request) {
        ParameterViewResponse data = parameterUseCase.init(request);
        return ApiResponseFactory.success(data, "Lista de parametros y resumen cargados correctamente");
    }

    @PostMapping("/init-form")
    public ResponseEntity<ApiResponse<ParameterFormResponse>> initParameterFormData(@RequestBody ParameterFormRequest request) {
        ParameterFormResponse data = parameterUseCase.initFormData(request);
        return ApiResponseFactory.success(data, "Datos de inicialización del formulario cargados correctamente");
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ApiResponse<Parameter>> getRoleById(@PathVariable Long id) {
        Parameter parameter = parameterUseCase.getParameterById(id);
        return ApiResponseFactory.success(parameter, "Parámetro obtenido correctamente");
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Parameter>> create(@Valid @RequestBody ParameterCreateRequest request) {
        Parameter data = parameterUseCase.create(request);
        return ApiResponseFactory.created(data, "Parámetro creado correctamente");
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Parameter>> update(@Valid @RequestBody ParameterUpdateRequest request) {
        Parameter data = parameterUseCase.update(request);
        return ApiResponseFactory.success(data, "Parámetro actualizado correctamente");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        boolean response = parameterUseCase.delete(id);
        return ApiResponseFactory.success(response, "Parámetro eliminado correctamente");
    }

    @PutMapping("/update-status")
    public ResponseEntity<ApiResponse<Boolean>> updateStatus(@RequestBody Long id) {
        Boolean response = parameterUseCase.updateStatus(id);
        return ApiResponseFactory.success(response, "Estado de parámetro actualizado correctamente");
    }

    @PostMapping("/export-pdf")
    public ResponseEntity<InputStreamResource> generateRolesReport(@RequestBody ParameterViewRequest request) {
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
    public ResponseEntity<InputStreamResource> generateRolesExcel(@RequestBody ParameterViewRequest request) {
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
