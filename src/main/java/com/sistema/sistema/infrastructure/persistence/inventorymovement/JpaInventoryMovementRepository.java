package com.sistema.sistema.infrastructure.persistence.inventorymovement;

import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementDetailDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaInventoryMovementRepository extends JpaRepository<InventoryMovementEntity, Long> {

    @Query(value = """

    SELECT
        im.id,
        im.type,
        im.quantity,
        im.previous_stock,
        im.current_stock,
        im.reason,
        im.reference_type,
        im.reference_id,
        (im.deleted_at IS NOT NULL) AS deleted,

        p.id,
        p.code,
        p.name,

        im.created_at,
        pc.full_name,

        im.modified_at,
        pm.full_name,

        im.deleted_at,
        pd.full_name

    FROM inventory_movements im

    INNER JOIN products p
        ON p.id = im.product_id

    LEFT JOIN users uc
        ON uc.id = im.created_by

    LEFT JOIN persons pc
        ON pc.id = uc.person_id

    LEFT JOIN users um
        ON um.id = im.modified_by

    LEFT JOIN persons pm
        ON pm.id = um.person_id

    LEFT JOIN users ud
        ON ud.id = im.deleted_by

    LEFT JOIN persons pd
        ON pd.id = ud.person_id

    WHERE (:productId IS NULL OR im.product_id = :productId)

    ORDER BY im.created_at DESC

    """, nativeQuery = true)
    List<Object[]> findMovementDetail(@Param("productId") Long productId);


}