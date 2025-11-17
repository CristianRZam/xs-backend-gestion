package com.sistema.sistema.application.dto.response.product;

import com.sistema.sistema.application.dto.response.parameter.ParameterDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ProductViewResponse {
    private List<ProductDTO> products;
    private Long totalProducts;
    private Long activeProducts;
    private Long inactiveProducts;
    private Long totalStock;
    private List<ParameterDto> categories;
    private List<ParameterDto> unitMeasures;
    private List<ParameterDto> valuationMethods;
}
