package com.sistema.sistema.application.dto.response.user;

import com.sistema.sistema.application.dto.response.parameter.ParameterDto;
import com.sistema.sistema.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class UserViewResponse {
    private List<UserDto> users;
    private List<ParameterDto> typeDocuments;
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long totalAdmins;
}
