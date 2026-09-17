package com.sistema.sistema.application.dto.response.catalogconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogConfigDTO {

    private Long id;

    private String name;

    // PORTADA
    private Boolean showCover;
    private String coverImage;
    private String headerImage;

    // VISTA PRODUCTOS
    private String viewMode;
    private Integer columns;

    private Boolean showImage;
    private Boolean showPrice;
    private Boolean showPricePromo;
    private Boolean showDescription;
    private Boolean showCode;
    private Boolean cardBorder;
    private Boolean showStatus;

    // PAGINACIÓN
    private Integer productsPerPage;
    private Boolean showPageNumber;
    private Boolean showHeader;
    private Boolean showFooter;

    private Boolean active;
}