package com.sistema.sistema.application.dto.response.profile;

import com.sistema.sistema.application.dto.response.parameter.ParameterDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ProfileFormresponse {
    ProfileDTO user;
    List<ParameterDto> documentTypes;

}
