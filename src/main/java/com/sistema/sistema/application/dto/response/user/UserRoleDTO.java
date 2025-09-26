package com.sistema.sistema.application.dto.response.user;

import com.sistema.sistema.application.dto.response.role.RoleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleDTO {
    private Long id;
    private RoleDto role;
    private Boolean deleted;
}
