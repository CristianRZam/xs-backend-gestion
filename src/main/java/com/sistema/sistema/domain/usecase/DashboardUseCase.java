package com.sistema.sistema.domain.usecase;
import com.sistema.sistema.application.dto.response.dashboard.DashboardDTO;
import com.sistema.sistema.application.dto.response.dashboard.DashboardSellerDTO;
import java.time.LocalDate;
import java.util.List;
public interface DashboardUseCase {
    DashboardDTO getSummary(LocalDate fromDate, LocalDate toDate, String scope, Long sellerId);
    List<DashboardSellerDTO> getSellers();
}
