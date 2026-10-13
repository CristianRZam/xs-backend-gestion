package com.sistema.sistema.application.dto.response.dashboard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSellerDTO {
    private final Long id;
    private final String name;
}
