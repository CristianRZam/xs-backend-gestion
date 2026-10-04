package com.sistema.sistema.application.service;
import com.sistema.sistema.application.dto.response.dashboard.DashboardDTO;
import com.sistema.sistema.domain.repository.DashboardRepository;
import com.sistema.sistema.domain.usecase.DashboardUseCase;
import org.springframework.stereotype.Service;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import com.sistema.sistema.infrastructure.security.XsUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DashboardService implements DashboardUseCase {
    private final DashboardRepository repository;

    public DashboardService(DashboardRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDTO getSummary() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = SecurityUtil.getCurrentUserId();
        if (authentication == null || !authentication.isAuthenticated() || userId == null
                || !(authentication.getPrincipal() instanceof XsUserDetails user) || !user.isEnabled()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión.");
        }

        log.debug("Cargando dashboard para usuario {}", userId);
        return repository.getSummary();
    }
}
