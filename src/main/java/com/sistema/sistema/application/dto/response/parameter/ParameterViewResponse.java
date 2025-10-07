package com.sistema.sistema.application.dto.response.parameter;

import com.sistema.sistema.domain.model.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ParameterViewResponse {
    List<ParameterDto> parameters;
    List<ParameterDto>  typesParameter;
    private Long totalParameters;
    private Long activeParameters;
    private Long inactiveParameters;
    private Long parametersWithParent;
}
