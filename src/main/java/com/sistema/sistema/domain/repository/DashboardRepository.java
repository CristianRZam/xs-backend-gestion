package com.sistema.sistema.domain.repository;
import com.sistema.sistema.application.dto.response.dashboard.DashboardDTO;
public interface DashboardRepository {
    DashboardDTO getSummary();
}
