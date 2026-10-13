package com.sistema.sistema.application.service;
import com.sistema.sistema.application.dto.response.dashboard.DashboardDTO;
import com.sistema.sistema.application.dto.response.dashboard.DashboardSellerDTO;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class DashboardService implements DashboardUseCase {
    private final DashboardRepository repository;

    public DashboardService(DashboardRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDTO getSummary(LocalDate fromDate, LocalDate toDate, String requestedScope, Long requestedSellerId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = SecurityUtil.getCurrentUserId();
        if (authentication == null || !authentication.isAuthenticated() || userId == null
                || !(authentication.getPrincipal() instanceof XsUserDetails user) || !user.isEnabled()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión.");
        }

        LocalDate from = fromDate == null ? LocalDate.now() : fromDate;
        LocalDate to = toDate == null ? from : toDate;
        if (from.isAfter(to)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La fecha inicial no puede ser posterior a la fecha final.");
        }
        if (ChronoUnit.DAYS.between(from, to) > 365) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El rango máximo permitido es de 366 días.");
        }

        boolean globalAllowed = SecurityUtil.hasAuthority("VIEW_DASHBOARD");
        boolean ownAllowed = globalAllowed || SecurityUtil.hasAuthority("VIEW_OWN_DASHBOARD");
        if (!ownAllowed) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No tienes permiso para visualizar el dashboard.");
        }

        boolean ownScope = "MY_SALES".equalsIgnoreCase(requestedScope);
        if (ownScope || !globalAllowed) {
            log.debug("Cargando dashboard personal para usuario {} entre {} y {}", userId, from, to);
            return repository.getSummary(from, to, userId, "MY_SALES");
        }

        log.debug("Cargando dashboard global entre {} y {} para vendedor {}", from, to, requestedSellerId);
        return repository.getSummary(from, to, requestedSellerId, requestedSellerId == null ? "GLOBAL" : "SELLER");
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardSellerDTO> getSellers() {
        if (!SecurityUtil.hasAuthority("VIEW_DASHBOARD")) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No tienes permiso para consultar vendedores.");
        }
        return repository.getActiveSellers();
    }
}
