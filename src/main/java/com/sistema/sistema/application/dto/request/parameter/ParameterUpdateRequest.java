package com.sistema.sistema.application.dto.request.parameter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ParameterUpdateRequest {
    @NotNull(message = "El id es obligatorio para la actualización")
    @Positive(message = "El id debe ser un número positivo")
    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 255, message = "El código no debe superar los 255 caracteres")
    private String code;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no debe superar los 255 caracteres")
    private String name;

    @Size(max = 255, message = "El nombre corto no debe superar los 255 caracteres")
    private String shortName;

    private Long parentParameterId;

    @NotNull(message = "El id del parámetro es obligatorio")
    private Long parameterId;

    @NotNull(message = "El tipo es obligatorio")
    private Long type;

    @NotNull(message = "El orden es obligatorio")
    private Long orderNumber;
}
