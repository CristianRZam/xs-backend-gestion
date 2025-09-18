package com.sistema.sistema.application.dto.response.user;

import com.sistema.sistema.domain.model.Parameter;
import com.sistema.sistema.domain.model.Person;
import com.sistema.sistema.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class UserFormResponse {
    User user;
    Person person;
    List<Parameter> documentTypes;
}
