package com.sistema.sistema.application.dto.request.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserCreateRequest {
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 50, message = "El nombre de usuario no debe superar los 50 caracteres")
    private String username;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Size(max = 100, message = "El correo electrónico  no debe superar los 100 caracteres")
    private String email;

    @NotNull(message = "El tipo de documento es obligatorio")
    @Min(value = 1, message = "El tipo de documento debe ser mayor a 0")
    private Long typeDocument;

    @NotBlank(message = "El nº de documento es obligatorio")
    @Size(max = 20, message = "El nº de documento no debe superar los 20 caracteres")
    private String document;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no debe superar los 255 caracteres")
    private String fullName;

    @Size(max = 50, message = "El nº de teléfono no debe superar los 50 caracteres")
    private String phone;

    @Size(max = 255, message = "La dirección no debe superar los 255 caracteres")
    private String address;

    @NotNull(message = "Debe seleccionar al menos un rol")
    private List<Long> roleIds;
}
