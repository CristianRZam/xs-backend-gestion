package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordFilterList;
import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordFormRequest;
import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordRequest;
import com.sistema.sistema.application.dto.request.birthrecord.BirthRecordUpdateRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordFormResponse;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordResponse;
import com.sistema.sistema.application.dto.response.birthrecord.BirthRecordViewResponse;
import com.sistema.sistema.application.report.BirthRecordReportService;
import com.sistema.sistema.application.report.ParameterReportService;
import com.sistema.sistema.domain.usecase.BirthRecordUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;


@RestController
@RequestMapping("/api/birthRecord")
public class BirthRecordController {

    private final BirthRecordUseCase birthRecordUseCase;
    private final BirthRecordReportService reportService;

    BirthRecordController(BirthRecordUseCase birthRecordUseCase, BirthRecordReportService reportService) {
        this.birthRecordUseCase = birthRecordUseCase;
        this.reportService = reportService;
    }

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<BirthRecordViewResponse>> init(@RequestBody BirthRecordFilterList request) {
        BirthRecordViewResponse data = birthRecordUseCase.init(request);
        return ApiResponseFactory.success(data, "Lista de partidas y resumen cargados correctamente");
    }

    @PostMapping("/init-form")
    public ResponseEntity<ApiResponse<BirthRecordFormResponse>> initFormData(@RequestBody BirthRecordFormRequest request) {
        BirthRecordFormResponse data = birthRecordUseCase.initFormData(request);
        return ApiResponseFactory.success(data, "Datos de inicialización del formulario cargados correctamente");
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BirthRecordResponse>> create(@RequestBody BirthRecordRequest request) {
        BirthRecordResponse data = birthRecordUseCase.create(request);
        return ApiResponseFactory.created(data, "Partrida de nacimiento creada correctamente");
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<BirthRecordResponse>> update(@RequestBody BirthRecordUpdateRequest request) {
        BirthRecordResponse data = birthRecordUseCase.update(request);
        return ApiResponseFactory.success(data, "Partrida de nacimiento actualizada correctamente");
    }

    @GetMapping("/certificate/{id}")
    public ResponseEntity<InputStreamResource> generateCertificate(@PathVariable Long id) {

        byte[] pdf = reportService.generateCertificate(id);

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdf));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificado-" + id + ".pdf")
                .contentType(MediaType.parseMediaType("application/pdf"))
                .contentLength(pdf.length)
                .body(resource);
    }

}
