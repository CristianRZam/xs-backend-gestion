package com.sistema.sistema.application.dto.response.product;

import com.sistema.sistema.application.dto.response.parameter.ParameterDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProductFormResponse {
    private ProductDTO product;
    private List<ParameterDto> categories;
    private List<ParameterDto> unitMeasures;
    private List<ParameterDto> valuationMethods;
}
