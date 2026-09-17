package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.response.productimage.StoredFileDTO;
import com.sistema.sistema.domain.usecase.FileStorageUseCase;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Profile("alfresco")
public class AlfrescoFileStorageService implements FileStorageUseCase {

    @Override
    public List<StoredFileDTO> upload(String folder, MultipartFile[] files) {
        // 1. Subir archivo a Alfresco
        // 2. Guardar nodeRef como storedName
        // 3. Guardar originalName como alt_text
        return List.of();
    }

    @Override
    public byte[] load(String folder, String filename) {
        // filename = nodeRef
        // Llamada REST a Alfresco
        // GET /alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/content
        return new byte[0];
    }
}
