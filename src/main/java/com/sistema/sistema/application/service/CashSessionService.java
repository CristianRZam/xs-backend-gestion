package com.sistema.sistema.application.service;

import com.sistema.sistema.domain.model.CashSession;
import com.sistema.sistema.application.dto.response.PageResponseDTO;
import com.sistema.sistema.domain.repository.CashSessionRepository;
import com.sistema.sistema.domain.usecase.CashSessionUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CashSessionService implements CashSessionUseCase {

    private final CashSessionRepository repository;

    public CashSessionService(CashSessionRepository repository) {
        this.repository = repository;
    }

    // ==========================================================
    // ABRIR SESIÓN
    // ==========================================================

    @Override
    public CashSession openSession(CashSession cashSession) {

        // No permitir abrir una nueva sesión
        // si ya existe una sesión abierta.
        if (repository.existsOpenSession()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe una sesión de caja abierta."
            );
        }

        if (cashSession.getOpeningAmount() == null) {
            cashSession.setOpeningAmount(BigDecimal.ZERO);
        }

        cashSession.setStatus("OPEN");

        return repository.openSession(cashSession);
    }

    // OBTENER SESIÓN ACTUAL
    @Override
    public CashSession getCurrentSession() {

        if (!repository.existsOpenSession()) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "No existe una sesión de caja abierta. Debe realizar la apertura de caja."
            );
        }

        return repository.getCurrentSession();

    }

    // ==========================================================
    // VERIFICAR SI EXISTE SESIÓN ABIERTA
    // ==========================================================

    @Override
    public boolean existsOpenSession() {
        return repository.existsOpenSession();
    }

    // ==========================================================
    // CERRAR SESIÓN
    // ==========================================================

    @Override
    public CashSession closeSession(
            Long id,
            BigDecimal closingAmount,
            BigDecimal expectedAmount,
            BigDecimal difference,
            String closingComment
    ) {

        if (!repository.existsOpenSession()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "No existe una sesión de caja abierta."
            );
        }

        if (closingAmount == null) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "El monto de cierre es obligatorio."
            );
        }

        CashSession current = repository.getCurrentSession();
        if (!current.getId().equals(id)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Solo puede cerrar la sesión de caja abierta.");
        }

        // El esperado es calculado en servidor: apertura + cobros en efectivo.
        expectedAmount = repository.calculateExpectedAmount(id);

        difference = closingAmount.subtract(expectedAmount);

        return repository.closeSession(
                id,
                closingAmount,
                expectedAmount,
                difference,
                closingComment
        );
    }

    // ==========================================================
    // HISTORIAL DE SESIONES
    // ==========================================================

    @Override
    public PageResponseDTO<CashSession> getHistory(int page, int size) {
        return repository.getHistory(page, size);
    }
}
