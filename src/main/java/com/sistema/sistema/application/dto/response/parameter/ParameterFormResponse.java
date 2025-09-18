package com.sistema.sistema.application.dto.response.parameter;

import com.sistema.sistema.domain.model.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ParameterFormResponse {
    Parameter parameter;
    List<Parameter> types;
}
