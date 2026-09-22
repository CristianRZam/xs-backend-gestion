package com.sistema.sistema.domain.repository;
import com.sistema.sistema.application.dto.response.dashboard.DashboardDTO;
import java.time.LocalDate;

public interface DashboardRepository {
    DashboardDTO getSummary();

    DashboardDTO getPersonalSummary(Long userId, LocalDate date);
}
