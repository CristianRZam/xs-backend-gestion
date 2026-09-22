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
import java.time.LocalDate;

@Service
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

        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().startsWith("ROLE_"));
        if (!hasRole) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No tiene un rol activo.");
        }

        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SUPER_ADMIN".equals(authority.getAuthority()));
        if (isSuperAdmin) {
            return repository.getSummary();
        }

        // Same server-local calendar as SaleDAOImpl's LocalDateTime.now().
        // The client never supplies the owner or the reporting scope.
        return repository.getPersonalSummary(userId, LocalDate.now());
    }
}
