package com.sistema.sistema.application.dto.request.order;

import com.sistema.sistema.domain.model.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemsCreateRequest {
    @NotEmpty(message = "Debe agregar al menos un producto.")
    private List<@Valid OrderItem> items;
}
