package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.request.product.ProductCreateRequest;
import com.sistema.sistema.application.dto.request.product.ProductFormRequest;
import com.sistema.sistema.application.dto.request.product.ProductUpdateRequest;
import com.sistema.sistema.application.dto.request.product.ProductViewRequest;
import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.product.ProductDTO;
import com.sistema.sistema.application.dto.response.product.ProductFormResponse;
import com.sistema.sistema.application.dto.response.product.ProductViewResponse;
import com.sistema.sistema.application.dto.response.productimage.ProductImageDTO;
import com.sistema.sistema.application.report.ProductReportService;
import com.sistema.sistema.domain.usecase.ProductUseCase;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductUseCase productUseCase;
    private final ProductReportService reportService;

    public ProductController(ProductUseCase productUseCase, ProductReportService reportService) {
        this.productUseCase = productUseCase;
        this.reportService = reportService;
    }

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<ProductViewResponse>> init(@Valid @RequestBody ProductViewRequest request) {
        ProductViewResponse result = productUseCase.init(request);
        return ApiResponseFactory.success(result, "Lista de productos y resumen cargados correctamente.");
    }

    @PostMapping("/init-form")
    public ResponseEntity<ApiResponse<ProductFormResponse>> initFormData(@RequestBody ProductFormRequest request) {
        ProductFormResponse data = productUseCase.initFormData(request);
        return ApiResponseFactory.success(data, "Datos de inicialización del formulario cargados correctamente");
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductDTO>> create(
            @Valid @RequestPart("product") ProductCreateRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestPart(value = "mainImageKey", required = false) String mainImageKey
    ) {
        ProductDTO product = productUseCase.create(request, images, mainImageKey);
        return ApiResponseFactory.created(product, "Producto creado correctamente");
    }



    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductDTO>> update(
            @Valid @RequestPart("product") ProductUpdateRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestPart(value = "imagesToKeep", required = false) List<ProductImageDTO> imagesToKeep,
            @RequestPart(value = "mainImageKey", required = false) String mainImageKey
    ) {
        ProductDTO product = productUseCase.update(request, images, imagesToKeep, mainImageKey);
        return ApiResponseFactory.success(product, "Producto actualizado correctamente");
    }



    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        boolean response = productUseCase.delete(id);
        return ApiResponseFactory.success(response, "Producto eliminado correctamente");
    }

    @PutMapping("/update-status")
    public ResponseEntity<ApiResponse<Boolean>> updateStatus(@RequestBody Long id) {
        boolean response = productUseCase.updateStatus(id);
        return ApiResponseFactory.success(response, "Estado de producto actualizado correctamente");
    }

    @PostMapping("/export-pdf")
    public ResponseEntity<InputStreamResource> exportPdf(@RequestBody ProductViewRequest request) {
        byte[] pdf = reportService.generatePdfReport(request);

        ByteArrayInputStream bis = new ByteArrayInputStream(pdf);
        InputStreamResource resource = new InputStreamResource(bis);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(resource);
    }

    @PostMapping("/export-excel")
    public ResponseEntity<InputStreamResource> exportExcel(@RequestBody ProductViewRequest request) {
        byte[] excel = reportService.generateExcelReport(request);

        ByteArrayInputStream bis = new ByteArrayInputStream(excel);
        InputStreamResource resource = new InputStreamResource(bis);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excel.length)
                .body(resource);
    }
}
