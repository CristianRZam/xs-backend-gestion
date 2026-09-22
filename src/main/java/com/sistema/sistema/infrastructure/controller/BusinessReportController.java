package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.request.report.ReportFilterRequest;
import com.sistema.sistema.application.report.BusinessReportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayInputStream;

@RestController @RequestMapping("/api/reports")
public class BusinessReportController {
    private final BusinessReportService service;
    public BusinessReportController(BusinessReportService service) { this.service = service; }
    @PostMapping("/{type}/excel") public ResponseEntity<InputStreamResource> excel(@PathVariable String type, @RequestBody ReportFilterRequest filter) { return file(service.excel(type,filter), type+"_reporte.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); }
    @PostMapping("/{type}/pdf") public ResponseEntity<InputStreamResource> pdf(@PathVariable String type, @RequestBody ReportFilterRequest filter) { return file(service.pdf(type,filter), type+"_reporte.pdf", MediaType.APPLICATION_PDF_VALUE); }
    private ResponseEntity<InputStreamResource> file(byte[] data,String name,String media) { return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+name).contentType(MediaType.parseMediaType(media)).contentLength(data.length).body(new InputStreamResource(new ByteArrayInputStream(data))); }
}
