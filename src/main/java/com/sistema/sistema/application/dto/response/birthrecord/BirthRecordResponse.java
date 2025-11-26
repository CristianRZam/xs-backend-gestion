package com.sistema.sistema.application.dto.response.birthrecord;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BirthRecordResponse {

    private Long id;

    private Integer year;
    private String actNumber;

    private String personName;
    private String sex;

    private String birthPlace;
    private LocalDateTime birthDatetime;

    private Long districtId;
    private String districtName;

    private Long fatherId;
    private String fatherName;

    private Long motherId;
    private String motherName;

    private String declarantName;
    private String declarantDocument;

    private String recordPlace;
    private LocalDateTime recordDatetime;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}