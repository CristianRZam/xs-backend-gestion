package com.sistema.sistema.application.dto.request.birthrecord;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BirthRecordRequest {

    private Integer year;
    private String actNumber;

    private String personName;
    private String sex;

    private String birthPlace;
    private LocalDateTime birthDatetime;

    private Long districtId;
    private String districtName;
    private String districtProvince;
    private String districtDepartment;

    private Long fatherId;
    private String fatherName;
    private String fatherNaturalOf;
    private String fatherNationality;
    private String fatherOccupation;
    private String fatherAddress;

    private Long motherId;
    private String motherName;
    private String motherNaturalOf;
    private String motherNationality;
    private String motherOccupation;
    private String motherAddress;

    private String declarantName;
    private String declarantDocument;

    private String recordPlace;
    private LocalDateTime recordDatetime;

    private Long createdBy;
}
