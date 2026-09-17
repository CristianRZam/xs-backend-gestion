package com.sistema.sistema.application.dto.response.product;

import com.sistema.sistema.application.dto.response.parameter.ParameterDto;
import com.sistema.sistema.application.dto.response.productimage.ProductImageDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProductFormResponse {
    private ProductDTO product;
    private List<ProductImageDTO> images;
    private List<ParameterDto> categories;
    private List<ParameterDto> unitMeasures;
    private List<ParameterDto> valuationMethods;
}
