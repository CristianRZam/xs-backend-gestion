package com.sistema.sistema.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parameter {
    private Long id;
    private Long parentParameterId;
    private Long parameterId;
    private String code;
    private Long type;
    private String name;
    private String shortName;
    private Long orderNumber;
    private Boolean active;

    // Auditoría
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long modifiedBy;
    private LocalDateTime modifiedAt;
    private Long deletedBy;
    private LocalDateTime deletedAt;
}
