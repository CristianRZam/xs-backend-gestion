package com.sistema.sistema.application.dto.request.Permission;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionUpdateRequest {
    @NotNull(message = "roleId no puede ser nulo")
    private Long roleId;

    private List<Long> permissionIds;
}
