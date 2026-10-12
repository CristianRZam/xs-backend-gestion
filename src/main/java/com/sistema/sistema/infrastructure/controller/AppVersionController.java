package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.application.dto.response.ApiResponse;
import com.sistema.sistema.application.dto.response.AppVersionResponse;
import com.sistema.sistema.application.service.AppVersionService;
import com.sistema.sistema.infrastructure.util.ApiResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app-version")
@RequiredArgsConstructor
public class AppVersionController {
    private final AppVersionService service;

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<AppVersionResponse>> check(
            @RequestHeader(value = "X-App-Platform", required = false) String platform,
            @RequestHeader(value = "X-App-Build", required = false) String build) {
        return ApiResponseFactory.success(service.check(platform, build), "Versión de aplicación verificada.");
    }
}
