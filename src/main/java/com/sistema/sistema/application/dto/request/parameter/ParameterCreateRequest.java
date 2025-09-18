package com.sistema.sistema.application.dto.request.parameter;

import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class ParameterCreateRequest {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 255, message = "El código no debe superar los 255 caracteres")
    private String code;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no debe superar los 255 caracteres")
    private String name;

    @Size(max = 255, message = "El nombre corto no debe superar los 255 caracteres")
    private String shortName;

    private Long parentParameterId;

    private Long parameterId;

    @NotNull(message = "El tipo es obligatorio")
    private Long type;

    @NotNull(message = "El orden es obligatorio")
    private Long orderNumber;

}
