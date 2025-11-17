package com.sistema.sistema.application.dto.request.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ProductViewRequest {

    private String code;
    private String name;
    private String description;
    private List<Integer> categories;
    private List<Integer> unitMeasures;
    private List<Integer> valuationMethods;
    private Boolean manageVariant;
    private Integer minimumStock;
    private Integer maximumStock;
    private Boolean status;
    private int page = 0;
    private int size = 5;
}
