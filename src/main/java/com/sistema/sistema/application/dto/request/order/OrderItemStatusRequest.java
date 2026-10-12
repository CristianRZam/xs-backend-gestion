package com.sistema.sistema.application.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemStatusRequest {
    @NotBlank
    @Size(max = 20)
    private String status;

    @Size(max = 500)
    private String comment;
}
