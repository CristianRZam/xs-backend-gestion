package com.sistema.sistema.application.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class OrderItemsStatusRequest {
    @NotEmpty private List<@NotNull Long> itemIds;
    @NotBlank @Size(max = 20) private String status;
    @Size(max = 500) private String comment;
}
