package com.sistema.sistema.infrastructure.persistence.cash;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

    List<CashSessionEntity>
    findByDeletedAtIsNullOrderByIdDesc();
}
