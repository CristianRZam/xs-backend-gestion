package com.sistema.sistema.application.dto.response.birthrecord;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BirthRecordDetailDTO {

    private Long id;

    // Datos generales
    private Integer year;
    private String actNumber;

    private String personName;
    private String sex;

    private String birthPlace;
    private LocalDateTime birthDatetime;

    // Localidad
    private Long districtId;
    private String districtName;
    private String districtProvince;
    private String districtDepartment;

    // Datos del padre
    private Long fatherId;
    private String fatherName;
    private String fatherNaturalOf;
    private String fatherNationality;
    private String fatherOccupation;
    private String fatherAddress;

    // Datos de la madre
    private Long motherId;
    private String motherName;
    private String motherNaturalOf;
    private String motherNationality;
    private String motherOccupation;
    private String motherAddress;

    // Declarante
    private String declarantName;
    private String declarantDocument;

    // Expedición del acta
    private String recordPlace;
    private LocalDateTime recordDatetime;

}
