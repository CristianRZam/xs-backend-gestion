package com.sistema.sistema.infrastructure.controller;

import com.sistema.sistema.domain.usecase.FileStorageUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageUseCase fileStorageUseCase;

    public FileController(FileStorageUseCase fileStorageUseCase) {
        this.fileStorageUseCase = fileStorageUseCase;
    }

    @GetMapping(
            value = "/products/{productId}/{filename:.+}"
    )
    public ResponseEntity<byte[]> getProductImage(
            @PathVariable Long productId,
            @PathVariable String filename
    ) throws IOException {
        String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8);

        byte[] data = fileStorageUseCase.load(
                "products/" + productId,
                decodedFilename
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, resolveContentType(decodedFilename))
                .body(data);
    }

    private String resolveContentType(String filename) {
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "webp" -> "image/webp";
            case "gif" -> MediaType.IMAGE_GIF_VALUE;
            default -> MediaType.IMAGE_JPEG_VALUE;
        };
    }
}
