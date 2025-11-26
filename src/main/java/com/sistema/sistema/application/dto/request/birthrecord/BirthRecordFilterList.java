package com.sistema.sistema.application.dto.request.birthrecord;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BirthRecordFilterList {

    private Integer year;
    private String actNumber;

    private String personName;
    private String sex;

    private String fatherName;
    private String motherName;

    private LocalDateTime birthDateFrom;
    private LocalDateTime birthDateTo;

    private LocalDateTime recordDateFrom;
    private LocalDateTime recordDateTo;

}
