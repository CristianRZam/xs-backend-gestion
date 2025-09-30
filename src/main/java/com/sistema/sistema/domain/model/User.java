package com.sistema.sistema.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private Person person;
    private String username;
    private String email;
    private String avatarUrl;
    private String password;
    private Boolean active;
    private Boolean deleted;
    private Set<UserRole> roles;
}
