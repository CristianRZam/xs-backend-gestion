package com.sistema.sistema.application.dto.response.birthrecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class BirthRecordViewResponse {
    List<BirthRecordListDTO> birthRecords;
    private Long totalRecords;
    private Long recordsThisMonth;
    private Long maleCount;
    private Long femaleCount;
}
