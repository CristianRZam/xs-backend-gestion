package com.sistema.sistema.infrastructure.persistence.rolepermission;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class RolePermissionKey implements Serializable {
    private Long roleId;
    private Long permissionId;
}
