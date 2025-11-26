package com.sistema.sistema.domain.repository;

import com.sistema.sistema.infrastructure.persistence.parent.ParentEntity;

import java.util.Optional;

public interface ParentRepository {
    Optional<ParentEntity>findById(Long fatherId);

    ParentEntity save(ParentEntity father);
}
