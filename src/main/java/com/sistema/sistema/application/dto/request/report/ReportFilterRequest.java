package com.sistema.sistema.application.dto.request.report;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class ReportFilterRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}
