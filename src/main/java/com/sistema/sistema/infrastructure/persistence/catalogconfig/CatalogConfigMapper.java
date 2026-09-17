package com.sistema.sistema.infrastructure.persistence.catalogconfig;

import com.sistema.sistema.application.dto.response.catalogconfig.CatalogConfigDTO;
import org.springframework.stereotype.Component;

@Component
public class CatalogConfigMapper {

    public CatalogConfigDTO toDTOFromEntity(CatalogConfigEntity entity) {
        if (entity == null) {
            return null;
        }

        return CatalogConfigDTO.builder()
                .id(entity.getId())
                .name(entity.getName())

                // PORTADA
                .showCover(entity.getShowCover())
                .coverImage(entity.getCoverImage())
                .headerImage(entity.getHeaderImage())

                // VISTA PRODUCTOS
                .viewMode(entity.getViewMode())
                .columns(entity.getColumns())
                .showImage(entity.getShowImage())
                .showPrice(entity.getShowPrice())
                .showPricePromo(entity.getShowPricePromo())
                .showDescription(entity.getShowDescription())
                .showCode(entity.getShowCode())
                .cardBorder(entity.getCardBorder())
                .showStatus(entity.getShowStatus())

                // PAGINACIÓN
                .productsPerPage(entity.getProductsPerPage())
                .showPageNumber(entity.getShowPageNumber())
                .showHeader(entity.getShowHeader())
                .showFooter(entity.getShowFooter())

                // ESTADO
                .active(entity.getActive())

                .build();
    }
}