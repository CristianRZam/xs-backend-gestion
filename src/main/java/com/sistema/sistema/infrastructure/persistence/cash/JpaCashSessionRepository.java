package com.sistema.sistema.infrastructure.persistence.cash;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface JpaCashSessionRepository extends JpaRepository<CashSessionEntity, Long> {

    Optional<CashSessionEntity>
    findFirstByStatusAndDeletedAtIsNullOrderByIdDesc(String status);

    @Query(value = """
        SELECT
            p_opened.full_name AS openedByName,
            p_closed.full_name AS closedByName
        FROM cash_sessions cs

        INNER JOIN users u_opened
            ON u_opened.id = cs.opened_by

        INNER JOIN persons p_opened
            ON p_opened.id = u_opened.person_id

        LEFT JOIN users u_closed
            ON u_closed.id = cs.closed_by

        LEFT JOIN persons p_closed
            ON p_closed.id = u_closed.person_id

        WHERE cs.id = :id
        """, nativeQuery = true)
    Optional<CashSessionUserNamesProjection> findUserNamesBySessionId(
            @Param("id") Long id
    );

    boolean existsByStatusAndDeletedAtIsNull(String status);

    List<CashSessionEntity> findByStatusAndDeletedAtIsNullAndOpenedAtBefore(
            String status,
            LocalDateTime openedBefore
    );

    List<CashSessionEntity>
    findByDeletedAtIsNullOrderByIdDesc();

    @Query(value = """
            SELECT COALESCE(SUM(p.amount), 0)
            FROM payments p INNER JOIN sales s ON s.id = p.sale_id
            WHERE p.cash_session_id = :sessionId
              AND s.status = 'COMPLETED' AND s.deleted_at IS NULL
              AND p.payment_method = 'CASH'
            """, nativeQuery = true)
    BigDecimal sumCashPaymentsBySessionId(@Param("sessionId") Long sessionId);
}
