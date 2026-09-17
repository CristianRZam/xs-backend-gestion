package com.sistema.sistema.infrastructure.persistence.productimage;

import com.sistema.sistema.application.common.FilePublicUrlBuilder;
import com.sistema.sistema.application.dto.response.productimage.ProductImageDTO;
import org.springframework.stereotype.Component;

@Component
public class ProductImageMapper {

    private final FilePublicUrlBuilder urlBuilder;

    public ProductImageMapper(FilePublicUrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    public ProductImageDTO toDto(ProductImageEntity entity) {

        ProductImageDTO dto = new ProductImageDTO();
        dto.setId(entity.getId());

        dto.setImageUrl(
                urlBuilder.productImage(
                        entity.getProductId(),
                        entity.getImageUrl()
                )
        );

        dto.setIsMain(entity.getIsMain());
        dto.setOrderNumber(entity.getOrderNumber());
        dto.setAltText(entity.getAltText());

        return dto;
    }
}
