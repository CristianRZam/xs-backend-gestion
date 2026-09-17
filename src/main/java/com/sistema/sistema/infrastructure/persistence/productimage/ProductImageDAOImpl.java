package com.sistema.sistema.infrastructure.persistence.productimage;

import com.sistema.sistema.application.dto.response.productimage.ProductImageDTO;
import com.sistema.sistema.application.dto.response.productimage.StoredFileDTO;
import com.sistema.sistema.domain.repository.ProductImageRepository;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductImageDAOImpl implements ProductImageRepository {
    private final JpaProductImageRepository jpa;
    private final ProductImageMapper mapper;

    public ProductImageDAOImpl(JpaProductImageRepository jpa, ProductImageMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<ProductImageDTO> saveImages(Long productId, List<StoredFileDTO> images) {

        int order = jpa.countByProductIdAndActiveTrue(productId) + 1;
        Long currentUserId = SecurityUtil.getCurrentUserId();

        List<ProductImageDTO> result = new ArrayList<>();

        for (StoredFileDTO img : images) {
            ProductImageEntity entity = ProductImageEntity.builder()
                    .productId(productId)
                    .imageUrl(img.getStoredName())
                    .altText(img.getOriginalName())
                    .isMain(false)
                    .orderNumber(order++)
                    .active(true)
                    .createdBy(currentUserId)
                    .createdAt(LocalDateTime.now())
                    .build();

            jpa.save(entity);
            result.add(mapper.toDto(entity));
        }

        return result;
    }

    @Override
    public void clearMainByProduct(Long productId) {
        jpa.clearMainByProduct(productId);
    }

    @Override
    public void markAsMain(Long imageId) {
        jpa.findById(imageId).ifPresent(entity -> {
            entity.setIsMain(true);
            jpa.save(entity);
        });
    }


    @Override
    public List<ProductImageDTO> findByProductIdAndActiveTrue(Long productId) {

        return jpa.findByProductIdAndActiveTrueOrderByOrderNumberAsc(productId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<ProductImageDTO> findByProductId(Long productId) {
        return jpa.findByProductId(productId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        jpa.findById(id).ifPresent(entity -> {
            entity.setActive(false);
            entity.setDeletedBy(currentUserId);
            entity.setDeletedAt(LocalDateTime.now());
            jpa.save(entity);
        });
    }


}
