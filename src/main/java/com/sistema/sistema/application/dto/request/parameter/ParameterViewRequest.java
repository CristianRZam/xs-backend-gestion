package com.sistema.sistema.application.dto.request.parameter;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ParameterViewRequest {
    private String name;
    private String shortName;
    private String code;
    private long type;
    private int page = 0;
    private int size = 5;
}
