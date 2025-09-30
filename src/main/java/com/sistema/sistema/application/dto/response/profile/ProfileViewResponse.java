package com.sistema.sistema.application.dto.response.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ProfileViewResponse {
    private ProfileDTO user;
}
