package com.sistema.sistema.application.dto.response.permission;

import com.sistema.sistema.domain.model.Permission;
import com.sistema.sistema.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionViewResponse {
    private Role role;
    private List<Permission> allPermissions;
    private List<Permission> assignedPermissions;
}
