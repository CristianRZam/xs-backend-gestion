package com.sistema.sistema.application.dto.request.Role;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoleViewRequest {
    private String name;
    private String description;
    private Boolean status;
    private int page = 0;
    private int size = 5;
}
