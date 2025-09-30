package com.sistema.sistema.application.dto.request.profile;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotNull(message = "El id es obligatorio para la actualización")
    @Positive(message = "El id debe ser un número positivo")
    private Long idUser;

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
}
