package com.sistema.sistema.application.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.sistema.sistema.application.dto.response.productimage.StoredFileDTO;
import com.sistema.sistema.domain.usecase.FileStorageUseCase;
import com.sistema.sistema.infrastructure.storage.ProductImageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Profile("azure")
@Slf4j
public class AzureBlobStorageService implements FileStorageUseCase {

    private final BlobContainerClient container;
    private final ProductImageProcessor productImageProcessor;

    public AzureBlobStorageService(BlobContainerClient container, ProductImageProcessor productImageProcessor) {
        this.container = container;
        this.productImageProcessor = productImageProcessor;
    }

    @Override
    public List<StoredFileDTO> upload(String folder, MultipartFile[] files) throws IOException {
        String uploadId = UUID.randomUUID().toString();
        List<StagedAzureImage> stagedImages = new ArrayList<>();
        List<BlobClient> storedBlobs = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                ProductImageProcessor.ProcessedImage image = productImageProcessor.process(file);
                String storedName = UUID.randomUUID() + ".webp";
                BlobClient temporaryBlob = container.getBlobClient(".tmp/products/" + uploadId + "/" + storedName);
                temporaryBlob.upload(new ByteArrayInputStream(image.content()), image.content().length, true);
                temporaryBlob.setHttpHeaders(new BlobHttpHeaders().setContentType("image/webp"));
                stagedImages.add(new StagedAzureImage(
                        temporaryBlob,
                        new StoredFileDTO(storedName, image.originalName())
                ));
            }

            for (StagedAzureImage stagedImage : stagedImages) {
                BlobClient finalBlob = container.getBlobClient(folder + "/" + stagedImage.file().getStoredName());
                byte[] content = stagedImage.temporaryBlob().downloadContent().toBytes();
                finalBlob.upload(new ByteArrayInputStream(content), content.length, true);
                finalBlob.setHttpHeaders(new BlobHttpHeaders().setContentType("image/webp"));
                storedBlobs.add(finalBlob);
            }

            log.debug("{} imágenes de producto almacenadas en Azure Blob Storage en {}", stagedImages.size(), folder);
            return stagedImages.stream().map(StagedAzureImage::file).toList();
        } catch (Exception exception) {
            storedBlobs.forEach(this::deleteQuietly);
            if (exception instanceof IOException ioException) {
                throw ioException;
            }
            throw exception;
        } finally {
            stagedImages.forEach(image -> deleteQuietly(image.temporaryBlob()));
        }
    }

    @Override
    public byte[] load(String folder, String filename) {
        String blobName = folder + "/" + filename;
        BlobClient blob = container.getBlobClient(blobName);
        return blob.downloadContent().toBytes();
    }

    private void deleteQuietly(BlobClient blob) {
        try {
            blob.deleteIfExists();
        } catch (Exception exception) {
            log.warn("No se pudo eliminar un blob temporal de producto", exception);
        }
    }

    private record StagedAzureImage(BlobClient temporaryBlob, StoredFileDTO file) {
    }
}

