package com.sistema.sistema.application.dto.request.catalogconfig;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CatalogConfigUpdateRequest {

    @NotNull(message = "El id es obligatorio")
    @Positive(message = "El id debe ser mayor a 0")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no debe superar los 100 caracteres")
    private String name;

    // PORTADA

    private Boolean showCover;

    @Size(max = 500, message = "La imagen de portada no debe superar los 500 caracteres")
    private String coverImage;

    @Size(max = 500, message = "La imagen del encabezado no debe superar los 500 caracteres")
    private String headerImage;

    // VISTA PRODUCTOS

    @NotBlank(message = "El modo de vista es obligatorio")
    private String viewMode;

    @Min(value = 1, message = "Las columnas deben ser mayores a 0")
    private Integer columns;

    private Boolean showImage;

    private Boolean showPrice;

    private Boolean showPricePromo;

    private Boolean showDescription;

    private Boolean showCode;

    private Boolean cardBorder;

    private Boolean showStatus;

    // PAGINACIÓN

    @Min(value = 1, message = "Los productos por página deben ser mayores a 0")
    private Integer productsPerPage;

    private Boolean showPageNumber;

    private Boolean showHeader;

    private Boolean showFooter;
}