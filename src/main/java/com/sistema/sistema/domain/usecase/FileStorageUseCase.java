package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.response.productimage.StoredFileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileStorageUseCase {
    List<StoredFileDTO> upload(String folder, MultipartFile[] files) throws IOException;

    byte[] load(String s, String filename) throws IOException;
}
