package com.sistema.sistema.application.dto.request.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordRequest {
    @NotBlank(message = "La contraseña antigua es obligatoria.")
    @Size(max = 255, message = "La contraseña antigua no debe superar los 255 caracteres")
    private String oldPassword;

    @NotBlank(message = "La contraseña nueva es obligatoria.")
    @Size(max = 255, message = "La contraseña nueva no debe superar los 255 caracteres")
    private String newPassword;

    @NotBlank(message = "La contraseña de confirmación es obligatoria.")
    @Size(max = 255, message = "La contraseña de confirmación  no debe superar los 255 caracteres")
    private String confirmationPassword;
}
