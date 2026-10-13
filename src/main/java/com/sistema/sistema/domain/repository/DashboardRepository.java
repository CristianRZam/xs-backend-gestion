package com.sistema.sistema.domain.repository;
import com.sistema.sistema.application.dto.response.dashboard.DashboardDTO;
import com.sistema.sistema.application.dto.response.dashboard.DashboardSellerDTO;
import java.time.LocalDate;
import java.util.List;
public interface DashboardRepository {
    DashboardDTO getSummary(LocalDate fromDate, LocalDate toDate, Long sellerId, String scope);
    List<DashboardSellerDTO> getActiveSellers();
}
