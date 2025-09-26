package com.sistema.sistema.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {
    private Long id;
    private Role role;
    private Boolean deleted;
}
