package com.sistema.sistema.infrastructure.persistence.sale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaSaleCancellationRepository extends JpaRepository<SaleCancellationEntity, Long> {
    Optional<SaleCancellationEntity> findBySaleId(Long saleId);

    @Query(value = """
            SELECT p.full_name
            FROM sale_cancellations sc
            INNER JOIN users u ON u.id = sc.cancelled_by
            INNER JOIN persons p ON p.id = u.person_id
            WHERE sc.sale_id = :saleId
            """, nativeQuery = true)
    Optional<String> findCancelledByName(@Param("saleId") Long saleId);
}
