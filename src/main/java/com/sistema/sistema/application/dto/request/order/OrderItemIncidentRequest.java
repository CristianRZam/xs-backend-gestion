package com.sistema.sistema.application.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemIncidentRequest {
    @NotBlank(message = "La resolución de la incidencia es obligatoria.")
    private String resolution;

    @NotBlank(message = "El motivo de la incidencia es obligatorio.")
    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres.")
    private String reason;

    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo.")
    private BigDecimal discountAmount;
}
