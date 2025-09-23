package com.sistema.sistema.application.dto.response.parameter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParameterDto {
    private Long id;
    private Long parentParameterId;
    private Long parameterId;
    private String code;
    private Long type;
    private String name;
    private String shortName;
    private Long orderNumber;
    private Boolean active;
    private Boolean deleted;

}
