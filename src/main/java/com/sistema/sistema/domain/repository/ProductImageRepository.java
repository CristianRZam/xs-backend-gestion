package com.sistema.sistema.domain.repository;

import com.sistema.sistema.application.dto.response.productimage.ProductImageDTO;
import com.sistema.sistema.application.dto.response.productimage.StoredFileDTO;

import java.util.List;

public interface ProductImageRepository {

    List<ProductImageDTO> saveImages(Long productId, List<StoredFileDTO> images);

    List<ProductImageDTO> findByProductIdAndActiveTrue(Long id);

    List<ProductImageDTO> findByProductId(Long id);

    void deleteById(Long id);

    void clearMainByProduct(Long productId);

    void markAsMain(Long imageId);
}
