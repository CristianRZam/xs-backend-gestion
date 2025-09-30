package com.sistema.sistema.application.dto.response.profile;

import com.sistema.sistema.domain.model.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ProfileDTO {
    private Long idUser;
    private String email;
    private String avatarUrl;
    private Boolean active;
    private Boolean deleted;
    private Person person;
    List<String> roles;
}
