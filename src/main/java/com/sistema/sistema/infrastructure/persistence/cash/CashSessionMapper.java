package com.sistema.sistema.infrastructure.persistence.cash;

import com.sistema.sistema.domain.model.CashSession;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CashSessionMapper {

    public CashSession toDomain(CashSessionEntity entity) {

        if (entity == null) {
            return null;
        }

        return CashSession.builder()
                .id(entity.getId())
                .cashRegisterId(entity.getCashRegisterId())
                .openedBy(entity.getOpenedBy())
                .openedAt(entity.getOpenedAt())
                .openingAmount(entity.getOpeningAmount())
                .closedBy(entity.getClosedBy())
                .closedAt(entity.getClosedAt())
                .expectedAmount(entity.getExpectedAmount())
                .closingAmount(entity.getClosingAmount())
                .difference(entity.getDifference())
                .status(entity.getStatus())
                .openingComment(entity.getOpeningComment())
                .closingComment(entity.getClosingComment())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .modifiedBy(entity.getModifiedBy())
                .modifiedAt(entity.getModifiedAt())
                .deleted(entity.getDeletedAt() != null)
                .build();
    }

    public CashSession addUserNames(
            CashSession session,
            CashSessionUserNamesProjection names
    ) {

        if (session == null || names == null) {
            return session;
        }

        session.setOpenedByName(names.getOpenedByName());
        session.setClosedByName(names.getClosedByName());

        return session;
    }

    public CashSessionEntity toEntity(CashSession domain) {

        if (domain == null) {
            return null;
        }

        return CashSessionEntity.builder()
                .id(domain.getId())
                .cashRegisterId(domain.getCashRegisterId())
                .openedBy(domain.getOpenedBy())
                .openedAt(domain.getOpenedAt())
                .openingAmount(domain.getOpeningAmount())
                .closedBy(domain.getClosedBy())
                .closedAt(domain.getClosedAt())
                .expectedAmount(domain.getExpectedAmount())
                .closingAmount(domain.getClosingAmount())
                .difference(domain.getDifference())
                .status(domain.getStatus())
                .openingComment(domain.getOpeningComment())
                .closingComment(domain.getClosingComment())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .modifiedBy(domain.getModifiedBy())
                .modifiedAt(domain.getModifiedAt())
                .build();
    }

    public List<CashSession> toDomainList(
            List<CashSessionEntity> entities
    ) {

        return entities.stream()
                .map(this::toDomain)
                .toList();
    }
}