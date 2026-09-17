package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.response.productimage.StoredFileDTO;
import com.sistema.sistema.domain.usecase.FileStorageUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Profile("local")
public class LocalFileStorageService implements FileStorageUseCase {

    @Value("${app.upload-dir}")
    private String basePath;

    @Override
    public List<StoredFileDTO> upload(String folder, MultipartFile[] files) throws IOException {
        List<StoredFileDTO> result = new ArrayList<>();

        Path dir = Paths.get(basePath, "uploads", folder);
        Files.createDirectories(dir);

        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();

            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String storedName = UUID.randomUUID() + extension;

            Files.copy(file.getInputStream(), dir.resolve(storedName));

            result.add(new StoredFileDTO(storedName, originalName));
        }

        return result;
    }

    @Override
    public byte[] load(String folder, String filename) throws IOException {
        Path file = Paths.get(basePath, "uploads", folder, filename);
        return Files.readAllBytes(file);
    }
}



