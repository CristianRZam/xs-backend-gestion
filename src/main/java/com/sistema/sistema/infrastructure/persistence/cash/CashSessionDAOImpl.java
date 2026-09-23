package com.sistema.sistema.infrastructure.persistence.cash;

import com.sistema.sistema.domain.model.CashSession;
import com.sistema.sistema.application.dto.response.PageResponseDTO;
import com.sistema.sistema.domain.repository.CashSessionRepository;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CashSessionDAOImpl implements CashSessionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final JpaCashSessionRepository jpa;
    private final CashSessionMapper mapper;

    public CashSessionDAOImpl(
            JpaCashSessionRepository jpa,
            CashSessionMapper mapper
    ) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    // ==========================================================
    // ABRIR SESIÓN
    // ==========================================================

    @Override
    public CashSession openSession(CashSession cashSession) {

        Long currentUserId = SecurityUtil.getCurrentUserId();

        CashSessionEntity entity = mapper.toEntity(cashSession);

        entity.setId(null);
        entity.setOpenedBy(currentUserId);
        entity.setOpenedAt(LocalDateTime.now());
        entity.setCreatedBy(currentUserId);
        entity.setCreatedAt(LocalDateTime.now());

        CashSessionEntity saved = jpa.save(entity);

        return mapper.toDomain(saved);
    }

    // ==========================================================
    // OBTENER SESIÓN ACTUAL
    // ==========================================================

    @Override
    public CashSession getCurrentSession() {

        CashSessionEntity entity = jpa.findFirstByStatusAndDeletedAtIsNullOrderByIdDesc("OPEN").orElseThrow(() ->
                        new EntityNotFoundException("No existe una sesión de caja abierta."));

        CashSession session = mapper.toDomain(entity);
        session.setExpectedAmount(calculateExpectedAmount(entity.getId()));

        jpa.findUserNamesBySessionId(entity.getId()).ifPresent(names -> mapper.addUserNames(session, names));

        return session;

    }

    // ==========================================================
    // VERIFICAR SI EXISTE SESIÓN ABIERTA
    // ==========================================================

    @Override
    public boolean existsOpenSession() {

        return jpa.existsByStatusAndDeletedAtIsNull("OPEN");
    }

    // ==========================================================
    // CERRAR SESIÓN
    // ==========================================================

    @Override
    public CashSession closeSession(
            Long id,
            java.math.BigDecimal closingAmount,
            java.math.BigDecimal expectedAmount,
            java.math.BigDecimal difference,
            String closingComment
    ) {

        CashSessionEntity entity = jpa.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Sesión de caja no encontrada con id: " + id
                        )
                );

        if (!"OPEN".equals(entity.getStatus())) {
            throw new IllegalStateException(
                    "La sesión de caja ya se encuentra cerrada."
            );
        }

        Long currentUserId = SecurityUtil.getCurrentUserId();

        entity.setClosedBy(currentUserId);
        entity.setClosedAt(LocalDateTime.now());
        entity.setExpectedAmount(expectedAmount);
        entity.setClosingAmount(closingAmount);
        entity.setDifference(difference);
        entity.setClosingComment(closingComment);
        entity.setStatus("CLOSED");

        entity.setModifiedBy(currentUserId);
        entity.setModifiedAt(LocalDateTime.now());

        CashSessionEntity saved = jpa.save(entity);

        return mapper.toDomain(saved);
    }

    // ==========================================================
    // HISTORIAL DE SESIONES
    // ==========================================================

    @Override
    public PageResponseDTO<CashSession> getHistory(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 100));
        var result = jpa.findByDeletedAtIsNullOrderByIdDesc(
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id")));
        return new PageResponseDTO<>(mapper.toDomainList(result.getContent()),
                result.getTotalElements(), safePage, safeSize, result.hasNext());
    }

    @Override
    public java.math.BigDecimal calculateExpectedAmount(Long sessionId) {
        CashSessionEntity session = jpa.findById(sessionId).orElseThrow(() ->
                new EntityNotFoundException("Sesión de caja no encontrada con id: " + sessionId));
        java.math.BigDecimal cashSales = jpa.sumCashPaymentsBySessionId(sessionId);
        return session.getOpeningAmount().add(
                cashSales == null ? java.math.BigDecimal.ZERO : cashSales
        );
    }
}
