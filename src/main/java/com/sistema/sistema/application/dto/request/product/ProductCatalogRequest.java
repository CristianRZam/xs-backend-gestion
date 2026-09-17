package com.sistema.sistema.application.dto.request.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCatalogRequest {

    private String name;
    private Boolean showCover;
    private String viewMode;
    private Integer columns;
    private Boolean showImage;
    private Boolean showPrice;
    private Boolean showPricePromo;
    private Boolean showDescription;
    private Boolean showCode;
    private Boolean showHeader;
    private Boolean showFooter;
    private Boolean showPageNumber;
    private Integer productsPerPage;
    private Boolean cardBorder;
    private Boolean showStatus;

}