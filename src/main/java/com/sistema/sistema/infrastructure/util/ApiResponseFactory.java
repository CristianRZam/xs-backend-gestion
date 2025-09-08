package com.sistema.sistema.infrastructure.util;

import com.sistema.sistema.application.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ApiResponseFactory {

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .status(HttpStatus.CREATED.value())
                .success(true)
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message, List<String> errors) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .status(status.value())
                .success(false)
                .message(message)
                .errors(errors)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}