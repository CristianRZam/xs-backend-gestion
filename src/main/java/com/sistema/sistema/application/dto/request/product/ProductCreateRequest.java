package com.sistema.sistema.application.dto.request.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductCreateRequest {

    @NotBlank(message = "El código del producto es obligatorio")
    @Size(max = 50, message = "El código del producto no debe superar los 50 caracteres")
    private String code;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 255, message = "El nombre del producto no debe superar los 255 caracteres")
    private String name;

    @NotBlank(message = "La descripción del producto es obligatoria")
    private String description;

    @NotNull(message = "La categoría del producto es obligatoria")
    @Min(value = 1, message = "La categoría del producto debe ser mayor a 0")
    private Long categoryId;

    @NotNull(message = "La unidad de medida del producto es obligatoria")
    @Min(value = 1, message = "La unidad de medida del producto debe ser mayor a 0")
    private Long unitMeasureId;

    @NotNull(message = "El método de valuación del producto es obligatoria")
    @Min(value = 1, message = "El método de valuación del producto debe ser mayor a 0")
    private Long valuationMethodId;

    @NotNull(message = "El precio de venta base es obligatorio")
    @Min(value = 0, message = "El precio de venta base no puede ser negativo")
    private BigDecimal basePrice;

    @Min(value = 0, message = "El precio promocional no puede ser negativo")
    private BigDecimal promoPrice;

    @NotNull(message = "El costo base es obligatorio")
    @Min(value = 0, message = "El costo base no puede ser negativo")
    private BigDecimal baseCost;
}
