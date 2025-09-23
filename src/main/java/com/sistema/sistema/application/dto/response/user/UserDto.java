package com.sistema.sistema.application.dto.response.user;

import com.sistema.sistema.application.dto.response.role.RoleDto;
import com.sistema.sistema.domain.model.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Boolean active;
    private Boolean deleted;
    private List<RoleDto> roles;
    private Person person;
}
