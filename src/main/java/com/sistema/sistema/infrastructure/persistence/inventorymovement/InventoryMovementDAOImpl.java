package com.sistema.sistema.infrastructure.persistence.inventorymovement;

import com.sistema.sistema.application.dto.request.inventorymovement.InventoryMovementCreateRequest;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementDTO;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementDetailDTO;
import com.sistema.sistema.domain.repository.IventoryMovementRepository;
import com.sistema.sistema.infrastructure.security.SecurityUtil;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class InventoryMovementDAOImpl implements IventoryMovementRepository {

    private final JpaInventoryMovementRepository jpa;
    private final InventoryMovementMapper mapper;

    public InventoryMovementDAOImpl(
            JpaInventoryMovementRepository jpa,
            InventoryMovementMapper mapper
    ) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<InventoryMovementDetailDTO> findPage(Long productId, int page, int size) {

        return jpa.findMovementDetail(productId, PageRequest.of(page, size))
                .stream()
                .map(r -> InventoryMovementDetailDTO.builder()

                        .id(((Number) r[0]).longValue())
                        .type((String) r[1])
                        .quantity((BigDecimal) r[2])
                        .previousStock((BigDecimal) r[3])
                        .currentStock((BigDecimal) r[4])
                        .reason((String) r[5])
                        .referenceType((String) r[6])
                        .referenceId(r[7] == null ? null : ((Number) r[7]).longValue())
                        .deleted((Boolean) r[8])

                        .productId(((Number) r[9]).longValue())
                        .productCode((String) r[10])
                        .productName((String) r[11])

                        .createdAt(
                                ((Timestamp) r[12]).toLocalDateTime()
                        )
                        .createdBy((String) r[13])

                        .modifiedAt(
                                r[14] == null
                                        ? null
                                        : ((Timestamp) r[14]).toLocalDateTime()
                        )
                        .modifiedBy((String) r[15])

                        .deletedAt(
                                r[16] == null
                                        ? null
                                        : ((Timestamp) r[16]).toLocalDateTime()
                        )
                        .deletedBy((String) r[17])

                        .build())
                .toList();
    }

    @Override
    public long countByProductId(Long productId) {
        return jpa.countByProductId(productId);
    }

    @Override
    public InventoryMovementDTO findById(Long id) {

        InventoryMovementEntity entity = jpa.findById(id)
                .orElse(null);

        return entity == null
                ? null
                : mapper.toDTOFromEntity(entity);
    }

    @Override
    public InventoryMovementDTO create(InventoryMovementCreateRequest request) {

        InventoryMovementEntity entity = new InventoryMovementEntity();

        entity.setProductId(request.getProductId());
        entity.setType(request.getType());
        entity.setQuantity(request.getQuantity());
        entity.setPreviousStock(request.getPreviousStock());
        entity.setCurrentStock(request.getCurrentStock());
        entity.setReason(request.getReason());
        entity.setReferenceType(request.getReferenceType());
        entity.setReferenceId(request.getReferenceId());

        entity.setCreatedBy(SecurityUtil.getCurrentUserId());

        InventoryMovementEntity saved = jpa.save(entity);

        return mapper.toDTOFromEntity(saved);
    }

}
