package com.sistema.sistema.application.dto.response;

import com.sistema.sistema.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class RoleViewResponse {
    private List<Role> roles;
    private Long totalRoles;
    private Long activeRoles;
    private Long inactiveRoles;
    private Long totalPermissions;
}
