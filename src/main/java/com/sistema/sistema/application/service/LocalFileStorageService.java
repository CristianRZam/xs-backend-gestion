package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.response.productimage.StoredFileDTO;
import com.sistema.sistema.domain.usecase.FileStorageUseCase;
import com.sistema.sistema.infrastructure.storage.ProductImageProcessor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LocalFileStorageService implements FileStorageUseCase {

    @Value("${app.upload-dir}")
    private String basePath;

    private final ProductImageProcessor productImageProcessor;

    public LocalFileStorageService(ProductImageProcessor productImageProcessor) {
        this.productImageProcessor = productImageProcessor;
    }

    @Override
    public List<StoredFileDTO> upload(String folder, MultipartFile[] files) throws IOException {
        Path dir = Paths.get(basePath, "uploads", folder);
        Path temporaryDir = Paths.get(basePath, "uploads", ".tmp", UUID.randomUUID().toString());
        Files.createDirectories(dir);
        Files.createDirectories(temporaryDir);
        List<StagedLocalImage> stagedImages = new ArrayList<>();
        List<Path> storedFiles = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                ProductImageProcessor.ProcessedImage image = productImageProcessor.process(file);
                String storedName = UUID.randomUUID() + ".webp";
                Path temporaryFile = temporaryDir.resolve(storedName);
                Files.write(temporaryFile, image.content());
                stagedImages.add(new StagedLocalImage(
                        temporaryFile,
                        new StoredFileDTO(storedName, image.originalName())
                ));
            }

            for (StagedLocalImage stagedImage : stagedImages) {
                Path finalFile = dir.resolve(stagedImage.file().getStoredName());
                Files.move(stagedImage.temporaryFile(), finalFile);
                storedFiles.add(finalFile);
            }

            log.debug("{} imágenes de producto almacenadas localmente en {}", stagedImages.size(), folder);
            return stagedImages.stream().map(StagedLocalImage::file).toList();
        } catch (Exception exception) {
            storedFiles.forEach(this::deleteQuietly);
            if (exception instanceof IOException ioException) {
                throw ioException;
            }
            throw exception;
        } finally {
            stagedImages.forEach(image -> deleteQuietly(image.temporaryFile()));
            deleteQuietly(temporaryDir);
        }
    }

    @Override
    public byte[] load(String folder, String filename) throws IOException {
        Path file = Paths.get(basePath, "uploads", folder, filename);
        return Files.readAllBytes(file);
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            log.warn("No se pudo eliminar un archivo temporal de producto", exception);
        }
    }

    private record StagedLocalImage(Path temporaryFile, StoredFileDTO file) {
    }
}



