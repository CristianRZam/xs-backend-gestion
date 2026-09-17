package com.sistema.sistema.application.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.sistema.sistema.application.dto.response.productimage.StoredFileDTO;
import com.sistema.sistema.domain.usecase.FileStorageUseCase;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Profile("azure")
public class AzureBlobStorageService implements FileStorageUseCase {

    private final BlobContainerClient container;

    public AzureBlobStorageService(BlobContainerClient container) {
        this.container = container;
    }

    @Override
    public List<StoredFileDTO> upload(String folder, MultipartFile[] files) throws IOException {
        List<StoredFileDTO> result = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            String storedName = UUID.randomUUID() + "-" + originalName;

            String blobName = folder + "/" + storedName;
            BlobClient blob = container.getBlobClient(blobName);
            blob.upload(file.getInputStream(), file.getSize(), true);

            result.add(new StoredFileDTO(storedName, originalName));
        }

        return result;
    }

    @Override
    public byte[] load(String folder, String filename) {
        String blobName = folder + "/" + filename;
        BlobClient blob = container.getBlobClient(blobName);
        return blob.downloadContent().toBytes();
    }
}

