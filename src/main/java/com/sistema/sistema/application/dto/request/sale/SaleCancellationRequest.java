package com.sistema.sistema.application.dto.request.sale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleCancellationRequest {
    @NotBlank(message = "El motivo de anulación es obligatorio.")
    @Size(max = 500, message = "El motivo de anulación no puede superar los 500 caracteres.")
    private String reason;
}
