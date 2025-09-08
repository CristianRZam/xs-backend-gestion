package com.sistema.sistema.application.dto.request.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleCreateRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no debe superar los 255 caracteres")
    private String name;

    @Size(max = 255, message = "La descripción no debe superar los 255 caracteres")
    private String description;
}