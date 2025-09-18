package com.sistema.sistema.application.dto.request.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserViewRequest {
    private String name;
    private int page = 0;
    private int size = 5;
}
