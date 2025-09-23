package com.sistema.sistema.application.dto.request.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserViewRequest {
    private List<Integer> typeDocuments;
    private String document;
    private String fullName;
    private String username;
    private String email;
    private Boolean status;
    private int page = 0;
    private int size = 5;
}
